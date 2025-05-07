package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.config.CacheConfig.CACHE_EPD_GROUPEMENTS_POINTS;
import static ca.qc.hydro.epd.enums.ConfigurationType.PDC101;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.cache.CacheManager;

import ca.qc.hydro.epd.dao.PointPrevisionDao;
import ca.qc.hydro.epd.dto.PointPrevisionDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dao.PointDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.NotificationMessage;
import ca.qc.hydro.epd.dto.PointSearchDto;
import ca.qc.hydro.epd.dto.QueryParam;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.notification.ENotificationType;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.service.wsclient.HttpClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PointService {

    private final PointRepository pointRepository;
    private final MessageSource messageSource;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CacheManager cacheManager;
    private final PointDao pointDao;
    private final PointPrevisionDao pointPrevisionDao;
    private final GroupePointService groupePointService;
    private final @Qualifier("calculConfigService")
    HttpClientService calculConfigService;

    public List<PointSearchDto> getPoints() {
        return pointDao.findPoints();
    }

    public List<PointPrevisionDto> getAllPointsForPrevision() {
        return pointPrevisionDao.findAllPoints();
    }

    public Point getOne(Long pointId) throws NotFoundException {
        return pointRepository.findById(pointId).orElseThrow(
                () -> new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{pointId}, messageSource))
        );
    }

    /**
     * Retourne la liste de tous les points par groupement.
     *
     * @return Une {@link List} de {@link Point}
     */
    public List<Point> getPointsByGrp(List<String> codesGrp) {
        return new ArrayList<>(pointRepository.findByGrp(codesGrp));
    }

    /**
     * Retourne la liste de tous les codes des points par groupement.
     *
     * @return Une {@link List} de {@link String}
     */
    public List<String> getCodesRefPointsByGrp(List<String> codesGrp) {
        return pointRepository.findByGrp(codesGrp).stream()
                .map(Point::getCodeRef)
                .toList();
    }

    /**
     * @param codeRefPoint codeRef du point
     * @return point
     * @throws NotFoundException exception si le point n'existe pas
     */
    public Point getOneByCodeRef(String codeRefPoint) throws NotFoundException {
        return pointRepository.findByCodeRef(codeRefPoint).orElseThrow(
                () -> new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{codeRefPoint}, messageSource))
        );
    }

    public List<Point> getByCodesRef(List<String> codesRef) {
        return pointRepository.findAllByCodeRef(codesRef);
    }

    /**
     * @param point nouveau Réseau
     * @return Le point persisté en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    @Transactional(rollbackFor = Exception.class)
    public Point create(Point point) throws ValidationException {
        // vérifier si un point ayant le même code existe déjà
        Optional<Point> pointOptionalByCode = pointRepository.findByCode(point.getCode());
        if (pointOptionalByCode.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_CODE_ALREADY_EXISTS, new Object[]{point.getNom()}, messageSource));
        }
        Optional<Point> pointOptionalByCodeRef = pointRepository.findByCodeRef(point.getCodeRef());
        if (pointOptionalByCodeRef.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_CODE_REF_ALREADY_EXISTS, new Object[]{point.getNom()}, messageSource));
        }

        return save(point);
    }

    /**
     * @param point Point modifié
     * @return Le point persisté en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    @Transactional(rollbackFor = Exception.class)
    public Point update(Point point) throws ValidationException {
        return save(point);
    }

    /**
     * @param codeRefPoint codeRef du point à supprimer
     * @throws NotFoundException   exception si le point n'existe pas
     * @throws ValidationException exception si la suppression a échoué
     */
    public void delete(String codeRefPoint) throws NotFoundException, ValidationException {
        Point point = getOneByCodeRef(codeRefPoint);
        try {
            pointRepository.deleteById(point.getId());
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_DELETE_ERROR, new Object[]{point.getCodeRef()}, messageSource));
        }
    }

    public void clearCacheAndSendNotification() throws ExecutionException, InterruptedException {
        cacheManager.getCache(CACHE_EPD_GROUPEMENTS_POINTS).clear();
        simpMessagingTemplate.convertAndSend(APIConstant.NOTIFICATION_REFRESH_LISTS, new NotificationMessage(ENotificationType.POINT, groupePointService.getWithDetails()));
    }

    /**
     * @param point point à enregistrer dans la BD
     * @return Le point persisté en BD
     * @throws ValidationException exception lors de l'enregistrement
     */
    private Point save(Point point) throws ValidationException {
        try {
            // Le code doit être en majuscule sans accents
            point.setCodeRef(StringUtils.stripAccents(point.getCodeRef()).toUpperCase());
            return pointRepository.save(point);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_SAVE_ERROR, new Object[]{point.getCodeRef()}, messageSource));
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
	        .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_METADATA_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class, new QueryParam("conf", PDC101.name()))
	        .exceptionally(e -> {
	            log.error("Erreur lors de la synchronisation des métadonnées PDC101) : {}", e.getMessage());
	            return null;
	        });

    	 CompletableFuture.allOf(synchSeuil, synchMetadata)
     		.thenAccept(response -> log.info("Synchronisation des seuils et des métadonnées (PDC101) terminée"));
    }
}
