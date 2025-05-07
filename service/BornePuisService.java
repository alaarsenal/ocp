package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.specification.BornePuisSpecifications.inCodesBornes;
import static ca.qc.hydro.epd.specification.BornePuisSpecifications.inNoMois;
import static ca.qc.hydro.epd.specification.BornePuisSpecifications.inPointIds;
import static ca.qc.hydro.epd.specification.BornePuisSpecifications.inTypesBornes;
import static org.springframework.data.jpa.domain.Specification.where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.BornePuis;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.BornePuisSearchCriteriaDto;
import ca.qc.hydro.epd.dto.PageRequestDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.BornePuisRepository;
import ca.qc.hydro.epd.service.wsclient.HttpClientService;
import ca.qc.hydro.epd.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BornePuisService {

    private final BornePuisRepository bornePuisRepository;
    private final MessageSource messageSource;
    private final PointService pointService;
    private final @Qualifier("calculConfigService")
    HttpClientService calculConfigService;

    @Transactional(readOnly = true)
    public Page<BornePuis> search(
            PageRequestDto pageRequestDto,
            List<String> codesGrp,
            List<String> codesRefPoints,
            List<String> codesBornes,
            List<String> typesBornes,
            List<Integer> noMois
    ) {
        List<Long> pointsIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(codesGrp) && CollectionUtils.isEmpty(codesRefPoints)) {
            pointsIds = pointService.getPointsByGrp(codesGrp).stream().map(Point::getId).toList();
        }
        if (!CollectionUtils.isEmpty(codesRefPoints)) {
            pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        }

        Specification<BornePuis> specification = where(inPointIds(pointsIds)
                .and(inCodesBornes(codesBornes))
                .and(inTypesBornes(typesBornes))
                .and(inNoMois(noMois)));

        Pageable pageable = PaginationUtils.getPageable(pageRequestDto);

        return bornePuisRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public List<BornePuis> search(BornePuisSearchCriteriaDto searchCriteria) {
        List<Long> pointsIds = pointService.getByCodesRef(searchCriteria.getCodesRefPoints()).stream().map(Point::getId).toList();
        return new ArrayList<>(bornePuisRepository.search(searchCriteria, pointsIds));
    }

    /**
     * @param id Long
     * @return borne de puissance
     * @throws NotFoundException exception si la borne de puissance n'existe pas
     */
    public BornePuis getOne(Long id) throws NotFoundException {
        return bornePuisRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.BORNE_PUIS_NOT_FOUND, new Object[]{id}, messageSource))
        );
    }

    /**
     * @param bornePuis nouvelle Borne de puissance
     * @return Le borne de puissance persistée en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    @Transactional(rollbackFor = Exception.class)
    public BornePuis create(BornePuis bornePuis) throws ValidationException {
        // vérifier si une borne de puissance ayant le même id existe déjà
        Optional<BornePuis> bornePuisOptional = bornePuisRepository.findByPointIdAndCodeBorneAndTypeBorneAndNoMois(
                bornePuis.getPointId(),
                bornePuis.getCodeBorne(),
                bornePuis.getTypeBorne(),
                bornePuis.getNoMois()
        );
        if (bornePuisOptional.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.BORNE_PUIS_ALREADY_EXISTS, new Object[]{bornePuis}, messageSource));
        }
        return save(bornePuis);
    }

    /**
     * @param bornePuis   nouvelle Borne de puissance
     * @param codesPoints Codes points pour lesquels on veut créer la borne de puissance
     * @param noMoisList  Mois pour lesquels on veut créer la borne de puissance
     * @return Le borne de puissance persistée en BD
     */
    @Transactional(rollbackFor = Exception.class)
    public BornePuis create(BornePuis bornePuis, List<String> codesPoints, List<Integer> noMoisList) throws ValidationException {
        if (!CollectionUtils.isEmpty(codesPoints) && !CollectionUtils.isEmpty(noMoisList)) {
            List<Point> points = pointService.getByCodesRef(codesPoints);
            for (Point point : points) {
                for (Integer noMois : noMoisList) {
                    BornePuis newBornePuis = BornePuis.builder()
                            .pointId(point.getId())
                            .codeBorne(bornePuis.getCodeBorne())
                            .typeBorne(bornePuis.getTypeBorne())
                            .noMois(noMois)
                            .valeur(bornePuis.getValeur())
                            .build();
                    try {
                        create(newBornePuis);
                    } catch (ValidationException e) {
                        log.error("Exception lors de L'enregistrement de la borne de puissance : {}", bornePuis);
                        throw e;
                    }
                }
            }
        }
        return bornePuis;
    }

    /**
     * @param id        id de la borne de puissance
     * @param bornePuis Borne de puissance modifiée
     * @return Le borne de puissance persistée en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    @Transactional(rollbackFor = Exception.class)
    public BornePuis update(Long id, BornePuis bornePuis) throws ValidationException, NotFoundException {
        // vérifier si la borne de puissance existe
        BornePuis oldBornePuis = getOne(id);
        oldBornePuis.setValeur(bornePuis.getValeur());
        return save(oldBornePuis);
    }

    /**
     * @param id id de la borne de puissance
     * @throws NotFoundException   exception si la borne de puissance n'existe pas
     * @throws ValidationException exception si la suppression a échoué
     */
    public void delete(Long id) throws NotFoundException, ValidationException {
        getOne(id);
        try {
            bornePuisRepository.deleteById(id);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.BORNE_PUIS_DELETE_ERROR, new Object[]{id}, messageSource));
        }
    }

    /**
     * @param bornePuis borne de puissance à enregistrer dans la BD
     * @return Le borne de puissance persistée en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    private BornePuis save(BornePuis bornePuis) throws ValidationException {
        try {
            return bornePuisRepository.save(bornePuis);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.BORNE_PUIS_SAVE_ERROR, new Object[]{bornePuis}, messageSource));
        }
    }

	public void synchronize() {
	    calculConfigService
    		.asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_BORNE_PUIS_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class)
			.exceptionally(e -> {
				log.error("Erreur lors de la synchronisation des bornes de puissance : {}", e.getMessage());
				return null;
			})
    		.thenAccept(response -> log.info("Synchronisation des bornes de puissance terminée"));
    }

}
