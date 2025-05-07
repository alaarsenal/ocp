package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.config.CacheConfig.CACHE_EPD_GROUPEMENTS_POINTS;
import static ca.qc.hydro.epd.specification.GroupePointSpecifications.inCodesGrp;
import static ca.qc.hydro.epd.specification.GroupePointSpecifications.inIndGrpAffich;
import static ca.qc.hydro.epd.specification.GroupePointSpecifications.inIndGrpSyst;
import static org.springframework.data.jpa.domain.Specification.where;
import static ca.qc.hydro.epd.enums.ConfigurationType.PDC103;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.cache.CacheManager;

import ca.qc.hydro.epd.dto.*;
import ca.qc.hydro.epd.enums.ETypeCalcul;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.ComposanteGroupement;
import ca.qc.hydro.epd.domain.GroupePoint;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.PrevisionEtudeSr;
import ca.qc.hydro.epd.domain.TypeCalc;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.GroupePointMapper;
import ca.qc.hydro.epd.notification.ENotificationType;
import ca.qc.hydro.epd.repository.GroupePointRepository;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.repository.PrevisionEtudeSrRepository;
import ca.qc.hydro.epd.repository.TypeCalcRepository;
import ca.qc.hydro.epd.service.wsclient.HttpClientService;
import ca.qc.hydro.epd.utils.Predicates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupePointService {

    private final MessageSource messageSource;

    private final GroupePointRepository groupePointRepository;
    private final PointRepository pointRepository;

    private final PrevisionEtudeSrRepository previsionEtudeSrRepository;
    private final TypeCalcRepository typeCalcRepository;

    private final GroupePointMapper groupePointMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CacheManager cacheManager;
    private final @Qualifier("calculConfigService")
    HttpClientService calculConfigService;

    @Value("${epd.features.previsionEtude}")
    private Boolean previsionEtudeEnabled;

    @Transactional(rollbackFor = Exception.class)
    public GroupePoint create(GroupePointDto dto) throws ValidationException {
        Optional<GroupePoint> optional = groupePointRepository.findById(dto.getCode());
        if (optional.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_ALREADY_EXISTS, messageSource));
        }

        GroupePoint grp = groupePointMapper.toEntity(dto);
        if (!CollectionUtils.isEmpty(grp.getComposantes())) {
            for (ComposanteGroupement composanteGroupement : grp.getComposantes()) {
                Optional<Point> optionalPoint = pointRepository.findById(composanteGroupement.getPointId());
                if (optionalPoint.isEmpty()) {
                    throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{composanteGroupement.getPointId()}, messageSource));
                }
                composanteGroupement.setCodeGroupe(grp.getCode());
                composanteGroupement.setPoint(optionalPoint.get());
            }
        }

        return groupePointRepository.saveAndFlush(grp);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String codeGrp) throws NotFoundException, ValidationException {
        Optional<GroupePoint> optional = groupePointRepository.findById(codeGrp);
        if (optional.isEmpty()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_NOT_FOUND, new Object[]{codeGrp}, messageSource));
        }
        GroupePoint groupePoint = optional.get();
        if (groupePoint.getIndGrpSyst() == 'O') {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_SYS_CANNOT_BE_DELETED, new Object[]{codeGrp}, messageSource));
        }
        List<TypeCalc> typeCalcList = typeCalcRepository.findByCodeGrp(codeGrp);
        if (!CollectionUtils.isEmpty(typeCalcList)) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_USED_IN_TYPE_CALC, new Object[]{codeGrp}, messageSource));
        }
        if (Boolean.TRUE.equals(previsionEtudeEnabled)) {
            List<PrevisionEtudeSr> previsionEtudeSrList = previsionEtudeSrRepository.findByCodeGrp(codeGrp);
            if (!CollectionUtils.isEmpty(previsionEtudeSrList)) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_USED_IN_PREV_ETUD, new Object[]{codeGrp}, messageSource));
            }
        }
        groupePointRepository.delete(optional.get());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMany(List<String> codesGrp) throws NotFoundException, ValidationException {
        if (!CollectionUtils.isEmpty(codesGrp)) {
            for (String codeGrp : codesGrp) {
                delete(codeGrp);
            }
        }
    }

    @Transactional(readOnly = true)
    public GroupePoint get(String codeGrp) throws NotFoundException {
        Optional<GroupePoint> optional = groupePointRepository.findByIdFetchComposantes(codeGrp);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_NOT_FOUND, new Object[]{codeGrp}, messageSource));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupePoint update(GroupePointDto dto) throws NotFoundException, ConcurrentEditionException, ValidationException {
        Optional<GroupePoint> optional = groupePointRepository.findById(dto.getCode());
        if (optional.isEmpty()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_NOT_FOUND, new Object[]{dto.getCode()}, messageSource));
        }

        GroupePoint grp = optional.get();
        // Valider si le groupement a été modifié depuis la dernière lecture (optimistic locking)
        if (!dto.getVersion().equals(grp.getDateMaj().toEpochSecond(ZoneOffset.UTC))) {
            throw new ConcurrentEditionException(ApiMessageFactory.getError(ApiMessageCode.GRP_POINT_MODIFIED_BY_OTHER_USER, messageSource));
        }
        // Mise à jour des infos du groupement point
        grp.setNom(dto.getNom());
        grp.setDescription(dto.getDescription());
        grp.setIndGrpSyst(dto.isIndGrpSyst() ? 'O' : 'N');
        grp.setIndGrpAffich(dto.isIndGrpAffich() ? 'O' : 'N');

        BigDecimal maxNoOrdreComposante = BigDecimal.ZERO;

        if (grp.getComposantes() != null) {
            maxNoOrdreComposante = grp.getComposantes().stream().max(Comparator.comparingInt(o -> o.getNoOrdreComposante().intValue()))
                    .map(ComposanteGroupement::getNoOrdreComposante)
                    .orElse(BigDecimal.ONE);
            grp.getComposantes().clear(); // Empty list... we'll update it with the new one next
        }

        if (!CollectionUtils.isEmpty(dto.getComposantes())) {
            for (ComposanteGroupementDto composanteGroupementDto : dto.getComposantes()) {
                Point point = pointRepository.findById(composanteGroupementDto.getPointId()).orElseThrow(
                        () -> new ValidationException(
                                ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{composanteGroupementDto.getCodeRefPoint()}, messageSource))
                );
                addComposante(grp, composanteGroupementDto.getNoOrdreComposante() + maxNoOrdreComposante.intValue(), point);
            }
        }
        return groupePointRepository.saveAndFlush(grp);
    }

    private void addComposante(GroupePoint groupePoint, Integer noOrdreComposante, Point point) {
        if (groupePoint.getComposantes() == null) {
            groupePoint.setComposantes(new HashSet<>());
        }
        groupePoint.getComposantes().add(ComposanteGroupement.builder()
                .codeGroupe(groupePoint.getCode())
                .pointId(point.getId())
                .noOrdreComposante(new BigDecimal(noOrdreComposante))
                .groupePoint(groupePoint)
                .point(point).build());
    }

    @Transactional(readOnly = true)
    public List<GroupePoint> search(GroupePointSearchCriteriaDto searchCriteria) {
        Specification<GroupePoint> specification = where(
                inCodesGrp(searchCriteria.getCodesGrp())
                        .and(inIndGrpSyst(searchCriteria.getIndGrpSyst()))
                        .and(inIndGrpAffich(searchCriteria.getIndGrpAffich()))
        );

        return groupePointRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    public List<GroupePointSearchDetailsResultDto> getWithDetails() throws ExecutionException, InterruptedException {
        StopWatch stopWatch = StopWatch.createStarted();

        List<GroupePointSearchDetailsResultDto> groupePointSearchDetailsResult = new ArrayList<>();

        CompletableFuture<List<GroupePointSearchDetailsResultDto>> groupementsPointsAsyncResult = CompletableFuture.supplyAsync(groupePointRepository::findGroupementsPoints);
        CompletableFuture<List<GroupePointSearchDetailsResultDto>> pointsModelesAsyncResult = CompletableFuture.supplyAsync(groupePointRepository::findPointsModelesFonctions);

        // Ajout des groupements et points au résultat
        List<GroupePointSearchDetailsResultDto> groupementsPointsResult = groupementsPointsAsyncResult.get();
        groupementsPointsResult.forEach(dto -> ajouterGroupementAvecPoints(groupePointSearchDetailsResult, groupementsPointsResult, dto));

        // Éliminer les points redondants
        List<GroupePointSearchDetailsResultDto> pointSearchDetailsResultDtosDistincts = new ArrayList<>(
                groupePointSearchDetailsResult.stream()
                        .filter(dto -> Objects.nonNull(dto.getCode()))
                        .filter(Predicates.distinctByKey(GroupePointSearchDetailsResultDto::getCode))
                        .sorted(Comparator.comparing(GroupePointSearchDetailsResultDto::getCode))
                        .toList());

        // Ajout des points orphelins
        List<GroupePointSearchDetailsResultDto> pointsOrphelins = groupementsPointsResult.stream()
                .filter(dto -> Objects.isNull(dto.getCode()))
                .toList();
        pointSearchDetailsResultDtosDistincts.addAll(pointsOrphelins);

        // Ajout des modèles et fonctions au résultat
        List<GroupePointSearchDetailsResultDto> pointsModelesResult = pointsModelesAsyncResult.get();
        pointSearchDetailsResultDtosDistincts.forEach(dto -> ajouterModelesEtFonctionsAuPoint(pointsModelesResult, dto));

        log.debug("Temps d'exécution getWithDetails : {} ms", stopWatch.getTime());
        return pointSearchDetailsResultDtosDistincts;
    }

    private void ajouterGroupementAvecPoints(
            List<GroupePointSearchDetailsResultDto> groupePointSearchDetailsResult,
            List<GroupePointSearchDetailsResultDto> groupementsPointsResult, GroupePointSearchDetailsResultDto dto
    ) {
        GroupePointSearchDetailsResultDto gr = GroupePointSearchDetailsResultDto.builder()
                .code(dto.getCode())
                .nom(dto.getNom())
                .description(dto.getDescription())
                .indGrpAffich(dto.getIndGrpAffich())
                .indGrpSyst(dto.getIndGrpSyst()).build();
        gr.setPoints(obtenirPointsPourGroupement(groupementsPointsResult, dto));
        groupePointSearchDetailsResult.add(gr);
    }

    private List<PointWithModelesDto> obtenirPointsPourGroupement(
            List<GroupePointSearchDetailsResultDto> groupementsPointsResult,
            GroupePointSearchDetailsResultDto dto
    ) {
        return groupementsPointsResult.stream()
                .filter(groupePointSearchResultDto -> Objects.nonNull(groupePointSearchResultDto.getCode()) && groupePointSearchResultDto.getCode().equals(dto.getCode()))
                .map(groupePointSearchResultDto -> PointWithModelesDto.builder()
                        .id(groupePointSearchResultDto.getPointId())
                        .code(groupePointSearchResultDto.getCodePoint())
                        .codeRef(groupePointSearchResultDto.getCodeRefPoint())
                        .nom(groupePointSearchResultDto.getNomPoint())
                        .description(groupePointSearchResultDto.getDescriptionPoint())
                        .seuilEcartPrev(groupePointSearchResultDto.getSeuilEcartPoint())
                        .build())
                .filter(pointWithModelesDto -> Objects.nonNull(pointWithModelesDto.getCodeRef()))
                .sorted(Comparator.comparing(PointWithModelesDto::getCodeRef))
                .collect(Collectors.toList());
    }

    private void ajouterModelesEtFonctionsAuPoint(List<GroupePointSearchDetailsResultDto> pointsModelesResult, GroupePointSearchDetailsResultDto dto) {
        if (Objects.nonNull(dto.getCode())) { // Points appartenant à un groupement
            dto.getPoints().forEach(point -> {
                ajouterModelesAuPoint(pointsModelesResult, point);
                point.getModeles().forEach(mod -> ajouterFonctionsAuModele(pointsModelesResult, point, mod));
            });
        } else { // Points orphelins
            PointWithModelesDto point = PointWithModelesDto.builder()
                    .id(dto.getPointId())
                    .code(dto.getCodePoint())
                    .codeRef(dto.getCodeRefPoint())
                    .modeles(new ArrayList<>())
                    .build();
            List<ModeleWithFonctions> modeles = getModelesPourPointSansGroupement(pointsModelesResult, dto);
            dto.setModeles(modeles);
            dto.getModeles().forEach(mod -> ajouterFonctionsAuModele(pointsModelesResult, point, mod));
        }
    }

    private List<ModeleWithFonctions> getModelesPourPointSansGroupement(List<GroupePointSearchDetailsResultDto> pointsModelesResult, GroupePointSearchDetailsResultDto dto) {
        return pointsModelesResult.stream()
                .filter(resMod -> dto.getPointId().equals(resMod.getPointId()))
                .map(resMod -> ModeleWithFonctions.builder()
                        .code(resMod.getCodeModele())
                        .nom(resMod.getNomModele())
                        .description(resMod.getDescriptionModele())
                        .fonctions(new ArrayList<>())
                        .build())
                .collect(Collectors.toList());
    }

    private void ajouterModelesAuPoint(List<GroupePointSearchDetailsResultDto> pointsModelesResult, PointWithModelesDto point) {
        List<GroupePointSearchDetailsResultDto> list = pointsModelesResult.stream()
                .filter(groupePointSearchResultDto -> groupePointSearchResultDto.getPointId().equals(point.getId()))
                .filter(Predicates.distinctByKey(GroupePointSearchDetailsResultDto::getCodeModele)).toList();
        point.setModeles(list.stream().map(groupePointSearchResultDto -> ModeleWithFonctions.builder()
                        .code(groupePointSearchResultDto.getCodeModele())
                        .nom(groupePointSearchResultDto.getNomModele())
                        .description(groupePointSearchResultDto.getDescriptionModele()).build())
                .collect(Collectors.toList()));
    }

    private void ajouterFonctionsAuModele(List<GroupePointSearchDetailsResultDto> pointsModelesResult, PointWithModelesDto point, ModeleWithFonctions mod) {
        Supplier<Stream<GroupePointSearchDetailsResultDto>> modeleFonctions = () -> pointsModelesResult.stream()
                .filter(groupePointSearchResultDto -> groupePointSearchResultDto.getPointId().equals(point.getId())
                        && groupePointSearchResultDto.getCodeModele().equals(mod.getCode()))
                .filter(Predicates.distinctByKey(GroupePointSearchDetailsResultDto::getCodeFonction));
        // Un modèle est réputé actif si au moins une fonction est active
        mod.setActive(modeleFonctions.get().anyMatch(modeleFonction -> Boolean.TRUE.equals(modeleFonction.getFonctionActive())));
        mod.setFonctions(modeleFonctions.get().map(searchDetailsResultDto -> FonctionDto.builder()
                        .code(searchDetailsResultDto.getCodeFonction())
                        .active(searchDetailsResultDto.getFonctionActive())
                        .build())
                .collect(Collectors.toList()));
    }

    public void clearCacheAndSendNotification() throws ExecutionException, InterruptedException {
        cacheManager.getCache(CACHE_EPD_GROUPEMENTS_POINTS).clear();
        simpMessagingTemplate.convertAndSend(APIConstant.NOTIFICATION_REFRESH_LISTS, new NotificationMessage(ENotificationType.GROUPEMENT, getWithDetails()));
    }

    public void synchronize() {

    	final var synchSeuil = calculConfigService
            .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_SEUILS_ECART_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class)
            .exceptionally(e -> {
                log.error("Erreur lors de la synchronisation des seuils ecart : {}", e.getMessage());
                return null;
            });

    	final var synchMetadata = calculConfigService
	        .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_METADATA_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class, new QueryParam("conf", PDC103.name()))
	        .exceptionally(e -> {
	            log.error("Erreur lors de la synchronisation des métadonnées (PDC103) : {}", e.getMessage());
	            return null;
	        });

        final var syncFoncRequises = calculConfigService
	        .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_FONCTIONS_REQUISES_ENDPOINT, SyncFonctionRequiseDto.builder().codTypPrev(ETypeCalcul.CYCL.getCode()).build(), Collections.emptyMap(), ApiResponse.class)
	        .exceptionally(e -> {
	            log.error("Erreur lors de la synchronisation des fonctions requises : {}", e.getMessage());
	            return null;
	        });

   	 CompletableFuture.allOf(synchSeuil, synchMetadata, syncFoncRequises)
    		.thenAccept(response -> log.info("Synchronisation des seuils, fonctions requises et des métadonnées (PDC103) terminée"));
    }
}
