package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.enums.ConfigurationType.PDC207;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.CleProduitCons;
import ca.qc.hydro.epd.domain.CleProduitConsId;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.ProduitId;
import ca.qc.hydro.epd.dto.CleProduitConsSearchCriteriaDto;
import ca.qc.hydro.epd.dto.QueryParam;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.CleProduitConsRepository;
import ca.qc.hydro.epd.service.wsclient.HttpClientService;
import ca.qc.hydro.epd.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-04
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CleProduitConsService {

    private final CleProduitConsRepository cleProduitConsRepository;
    private final PointService pointService;
    private final MessageSource messageSource;
    private final ProduitService produitService;
    private final @Qualifier("calculConfigService")
    HttpClientService calculConfigService;
    /**
     * @param page           numéro de page
     * @param size           taille de la page
     * @param sortField      champ de tri
     * @param sortOrder      ASC ou DESC
     * @param searchCriteria les critères de recherche
     * @return Une page d'objet CleProduitCons corresponsant aux critères de recherche
     */
    @Transactional(readOnly = true)
    public Page<CleProduitCons> getPage(
            int page,
            int size,
            String sortField,
            Sort.Direction sortOrder,
            CleProduitConsSearchCriteriaDto searchCriteria
    ) {

        if (!CollectionUtils.isEmpty(searchCriteria.getCodesGroupements()) && CollectionUtils.isEmpty(searchCriteria.getCodesRefPoints())) {
            searchCriteria.setCodesRefPoints(new ArrayList<>(pointService.getCodesRefPointsByGrp(searchCriteria.getCodesGroupements())));
        }

        Pageable pageable = PaginationUtils.getPageable(page, size, sortOrder, sortField);

        return cleProduitConsRepository.search(searchCriteria, pageable);
    }

    /**
     * Chercher un produit par son id
     *
     * @param id id à chercher
     * @return produit
     * @throws NotFoundException exception si le produit n'existe pas
     */
    public CleProduitCons getOne(CleProduitConsId id) throws NotFoundException {
        return cleProduitConsRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.CLEPRODUITCONS_NOT_FOUND, new Object[]{id}, messageSource))
        );
    }


    public CleProduitCons getOne(String codePoint, String codeSourceDonnee, String codeProduit, String noCle, OffsetDateTime dateEnr) throws NotFoundException {
        Point point = pointService.getOneByCodeRef(codePoint);
        CleProduitConsId id = CleProduitConsId.builder().pointId(point.getId()).codeSourceDonnee(codeSourceDonnee).codeProduit(codeProduit)
                .noCle(noCle).dateEnr(dateEnr.toLocalDateTime()).build();
        return getOne(id);
    }

    /**
     * @param cleProduitCons nouveau produit
     * @param codesPoints    Codes points pour lesquels on veut créer les cles produits
     * @return Le borne de puissance persistée en BD
     */
    @Transactional(rollbackFor = Exception.class)
    public CleProduitCons create(CleProduitCons cleProduitCons, List<String> codesPoints) throws ValidationException, NotFoundException {
        if (!CollectionUtils.isEmpty(codesPoints)) {
            List<Point> points = pointService.getByCodesRef(codesPoints);
            for (Point point : points) {
                CleProduitCons newCleProduitCons = CleProduitCons.builder()
                        .pointId(point.getId())
                        .codeSourceDonnee(cleProduitCons.getCodeSourceDonnee())
                        .codeProduit(cleProduitCons.getCodeProduit())
                        .noCle(cleProduitCons.getNoCle())
                        .dateEnr(LocalDateTime.now())
                        .cle(cleProduitCons.getCle())
                        .indChargement(cleProduitCons.getIndChargement())
                        .point(point)
                        .produit(produitService.getOne(
                                ProduitId.builder()
                                        .codeSource(cleProduitCons.getCodeSourceDonnee())
                                        .codeProduit(cleProduitCons.getCodeProduit())
                                        .build())
                        )
                        .build();
                try {
                    return save(newCleProduitCons);
                } catch (ValidationException e) {
                    log.error("Exception lors de L'enregistrement de produit : {}", cleProduitCons);
                    throw e;
                }
            }
        }
        return cleProduitCons;
    }

    /**
     * Modifier un produit
     *
     * @param id             id de produit
     * @param cleProduitCons Le produit modifié
     * @return Le produit modifié
     * @throws NotFoundException   exception si le produit n'existe pas
     * @throws ValidationException exception lors de l'enregistrement
     */
    @Transactional(rollbackFor = Exception.class)
    public CleProduitCons update(CleProduitConsId id, CleProduitCons cleProduitCons) throws NotFoundException, ValidationException {
        CleProduitCons oldProduitCons = getOne(id);
        oldProduitCons.setCle(cleProduitCons.getCle());
        oldProduitCons.setIndChargement(cleProduitCons.getIndChargement());
        return save(oldProduitCons);
    }

    @Transactional(rollbackFor = Exception.class)
    public CleProduitCons update(String codePoint, String codeSourceDonnee, String codeProduit, String noCle, OffsetDateTime dateEnr, CleProduitCons cleProduitCons) throws ValidationException, NotFoundException {
        Point point = pointService.getOneByCodeRef(codePoint);
        CleProduitConsId id = CleProduitConsId.builder().pointId(point.getId()).codeSourceDonnee(codeSourceDonnee).codeProduit(codeProduit)
                .noCle(noCle).dateEnr(dateEnr.toLocalDateTime()).build();
        cleProduitCons.setId(id);
        return update(id, cleProduitCons);
    }

    /**
     * Supprimer un produit
     *
     * @param id id
     */
    public void delete(CleProduitConsId id) throws NotFoundException, ValidationException {
        getOne(id);
        try {
            cleProduitConsRepository.deleteById(id);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.CLEPROSUITCONS_DELETE_ERROR, new Object[]{id}, messageSource));
        }
    }

    public void delete(String codePoint, String codeSourceDonnee, String codeProduit, String noCle, OffsetDateTime dateEnr) throws ValidationException, NotFoundException {
        Point point = pointService.getOneByCodeRef(codePoint);
        CleProduitConsId id = CleProduitConsId.builder().pointId(point.getId()).codeSourceDonnee(codeSourceDonnee).codeProduit(codeProduit)
                .noCle(noCle).dateEnr(dateEnr.toLocalDateTime()).build();
        delete(id);
    }

    /**
     * @param cleProduitCons produit à enregistrer dans la BD
     * @return Le produit persistée en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    private CleProduitCons save(CleProduitCons cleProduitCons) throws ValidationException {
        try {
            return cleProduitConsRepository.save(cleProduitCons);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.CLEPRODUITCONS_SAVE_ERROR, new Object[]{cleProduitCons}, messageSource));
        }
    }

    public void synchronize() {

        final var synchSeuil = calculConfigService
            .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_SEUILS_ECART_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class)
            .exceptionally(e -> {
                log.error("Erreur lors de la synchronisation des seuils ecart : {}", e.getMessage());
                return null;
            });

        final var synchMetadata = calculConfigService
	        .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_METADATA_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class, new QueryParam("conf", PDC207.name()))
	        .exceptionally(e -> {
	            log.error("Erreur lors de la synchronisation des métadonnées (PDC207) : {}", e.getMessage());
	            return null;
	        });

        CompletableFuture.allOf(synchSeuil, synchMetadata)
        	.thenAccept(response -> log.info("Synchronisation des seuils et des métadonnées (PDC207) terminée"));
    }
}
