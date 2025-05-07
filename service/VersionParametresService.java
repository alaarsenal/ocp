package ca.qc.hydro.epd.service;

import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.dto.QueryParam;
import ca.qc.hydro.epd.dto.SyncFonctionRequiseDto;
import ca.qc.hydro.epd.enums.ETypeCalcul;
import ca.qc.hydro.epd.service.wsclient.HttpClientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import ca.qc.hydro.epd.apierror.ApiMessage;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.ActionJournal;
import ca.qc.hydro.epd.domain.AssoVersionParametres;
import ca.qc.hydro.epd.domain.Journalisation;
import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.ValeurVp;
import ca.qc.hydro.epd.domain.VersionParametres;
import ca.qc.hydro.epd.dto.VersionParametresDto;
import ca.qc.hydro.epd.dto.VpDonneeParamUpdateDto;
import ca.qc.hydro.epd.dto.VpMiseEnExploitationDto;
import ca.qc.hydro.epd.dto.VpMiseEnExploitationResponseDto;
import ca.qc.hydro.epd.dto.VpSearchCriteriaDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.repository.AssoVersionParametresRepository;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.repository.ValeurVpRepository;
import ca.qc.hydro.epd.repository.VersionParametresRepository;
import ca.qc.hydro.epd.service.wsclient.NotifNouvDonneesService;
import ca.qc.hydro.epd.service.wsclient.dto.NotifNouvDonneesResponse;
import ca.qc.hydro.epd.utils.EtagUtils;
import ca.qc.hydro.security.helper.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VersionParametresService {

    private static final String TYPE_DONNE_VAL = "VAL";
    private static final Character ARCHIVEE = 'O';
    private final MessageSource messageSource;
    private final AssoVersionParametresRepository assoVersionParametresRepository;
    private final PointRepository pointRepository;
    private final VersionParametresRepository versionParametresRepository;
    private final ValeurVpRepository valeurVpRepository;
    private final NotifNouvDonneesService notifNouvDonneesService;
    private final JournalisationService journalisationService;
    private final ActionJournalService actionJournalService;
    private final @Qualifier("calculConfigService") HttpClientService calculConfigService;

    @Value("${hq.pdcalcul.vp.default-values.pas-de-temps:24}")
    private int defaultPasDeTemps;
    @Value("${hq.pdcalcul.vp.no-version-en-exploitation:0}")
    private int noVersionEnExploitation;

    @Transactional(rollbackFor = Exception.class)
    public List<VersionParametres> copy(VersionParametresDto copyDto) throws ValidationException, NotFoundException {
        // Validation de l'existence de la VP d'origine (dans le cas où elle aurait été supprimée par un autre utilisateur)
        long origId = copyDto.getVpId();
        VersionParametres origVp = versionParametresRepository.findById(origId).orElse(null);

        if (Objects.isNull(origVp)) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{origId}, messageSource));
        }

        if (!TYPE_DONNE_VAL.equals(origVp.getValeurVp().getCodTypeDonnee())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_COPY_TYPE_ADR_NOT_ALLOWED, new Object[]{origId}, messageSource));
        }

        List<VersionParametres> targetVpList = new ArrayList<>();

        if (copyDto.isCopyByGrp()) {
            // Copie par groupement
            List<Point> points = pointRepository.findByGrp(Collections.singletonList(copyDto.getCodeGrp()));
            if (CollectionUtils.isNotEmpty(points)) {
                for (Point point : points) {
                    // Pour chaque point faisant partie du groupement sélectionné, le système cherche la plus récente VP de même modèle, année, saison et
                    // numéro que la VP d’origine comme VP source et crée une nouvelle version de paramètres cible.
                    Optional<VersionParametres> latestOptional = versionParametresRepository.findLatest(point.getCodeRef(), origVp.getCodeModele(), origVp.getAnnee(),
                            origVp.getSaison(), origVp.getNoVersion()
                    );

                    if (latestOptional.isPresent()) {
                        targetVpList.add(createTargetVp(latestOptional.get(), copyDto));

                    } else {
                        // Impossible de créer les nouvelles versions de paramètres. Un des points du groupement n’a pas de version de paramètres source
                        // cette combinaison de Modèle, Année, Saison et Numéro
                        throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_COPY_MISSING_SOURCE_VERSION, new Object[]{point.getCodeRef(),
                                origVp.getCodeModele(), origVp.getAnnee().toString(), origVp.getSaison(), origVp.getNoVersion()}, messageSource));
                    }
                }

            } else {
                // Aucun point n'a été trouvé pour le groupement cible
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_COPY_TARGET_GRP_IS_EMPTY, new Object[]{copyDto.getCodeGrp()}, messageSource));
            }

        } else {
            // Copie par point
            targetVpList.add(createTargetVp(origVp, copyDto));
        }

        Iterable<VersionParametres> iterable = versionParametresRepository.saveAll(targetVpList);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    private VersionParametres createTargetVp(VersionParametres sourceVp, VersionParametresDto copyDto) throws ValidationException {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        VersionParametres targetVp = VersionParametres.builder()
                .pointId(sourceVp.getPointId())
                .codeModele(sourceVp.getCodeModele())
                .annee(copyDto.getAnnee())
                .saison(copyDto.getSaison())
                .noVersion(copyDto.getNoVersion())
                .dateEnregistrement(nowUtc)
                .valeurVp(sourceVp.getValeurVp())
                .indArchivee(sourceVp.getIndArchivee()).build();

        List<ApiMessage> messages = validateTargetVersion(sourceVp, targetVp);
        if (CollectionUtils.isNotEmpty(messages)) {
            throw new ValidationException(messages);
        }

        targetVp.setDescription(copyDto.getDescription());
        targetVp.setVersionOrig(sourceVp);
        targetVp.setPasDeTemps(defaultPasDeTemps);
        return targetVp;
    }

    private List<ApiMessage> validateTargetVersion(VersionParametres source, VersionParametres target) {
        List<ApiMessage> messages = new ArrayList<>();
        if (target.getPointId().equals(source.getPointId()) && target.getCodeModele().equals(source.getCodeModele()) && target.getAnnee().equals(source.getAnnee())
                && target.getSaison().equals(source.getSaison()) && target.getNoVersion().equals(source.getNoVersion())) {
            messages.addAll(ApiMessageFactory.getError(ApiMessageCode.VP_COPY_TARGET_VERSION_SAME_AS_SOURCE, messageSource));
        }
        return messages;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<VersionParametres> vps) throws ValidationException, NotFoundException {
        for (VersionParametres vp : vps) {
            log.debug(vp.toString());
            Optional<VersionParametres> vpOptional = versionParametresRepository.findById(vp.getVpId());

            if (vpOptional.isPresent()) {
                if (vpOptional.get().getNoVersion() == 0 || vpOptional.get().getNoVersion() == 90) {
                    throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_EN_EXPLOITATION_CANNOT_BE_DELETED, messageSource));
                }
                versionParametresRepository.delete(vpOptional.get());

            } else {
                throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{vp}, messageSource));
            }
        }
    }

    @Transactional(readOnly = true)
    public VersionParametres get(Long vpId) throws NotFoundException {
        Optional<VersionParametres> vpOptional = versionParametresRepository.findById(vpId);
        if (vpOptional.isPresent()) {
            return vpOptional.get();
        } else {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{vpId}, messageSource));
        }
    }

    /**
     * Retourne la liste des années distinctes pour lesquelles il existe au moins une version de paramètres en se basant
     * sur le ou les code(s) point(s) passé(s) en paramètre(s).
     *
     * @param codesRefPoints Les codes points pour lesquels on veut récupérer la liste des années.
     * @return Une {@link List} de {@link Integer} contenant la liste des années distinctes trouvées
     */
    public List<Integer> getAnneesByPoints(List<String> codesRefPoints) {
        return versionParametresRepository.findAnneesByPoints(codesRefPoints);
    }

    public List<Modele> getModelesByPointsAndAnnees(List<String> codesRefPoints, List<Integer> annees) {
        return versionParametresRepository.findModelesByPointsAndAnnees(codesRefPoints, annees);
    }

    public List<Character> getSaisonsByPoints(List<String> codesRefPoints) {
        return versionParametresRepository.findSaisonsByPoints(codesRefPoints);
    }

    public List<Integer> getVersionsByPoints(List<String> codesRefPoints) {
        return versionParametresRepository.findVersionsByPoints(codesRefPoints);
    }

    @Transactional(rollbackFor = Exception.class)
    public VersionParametres update(VersionParametresDto updatedVp) throws NotFoundException, ConcurrentEditionException, ValidationException {
        if (StringUtils.isEmpty(updatedVp.getDescription())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.REQUIRED_PARAMS, messageSource));
        }

        Optional<VersionParametres> vpOptional = versionParametresRepository.findById(updatedVp.getVpId());

        if (vpOptional.isEmpty()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{updatedVp}, messageSource));
        }

        VersionParametres vp = vpOptional.get();
        // Valider si la version de paramètres a été modifiée depuis la dernière lecture (optimistic locking)
        if (!EtagUtils.versionEstValide(updatedVp.getDateMaj(), vp.getDateMaj().toEpochSecond(ZoneOffset.UTC) * 1000)) {
            throw new ConcurrentEditionException(ApiMessageFactory.getError(ApiMessageCode.VP_MODIFIED_BY_OTHER_USER, messageSource));
        }

        vp.setDescription(updatedVp.getDescription());
        return vp;
    }

    @Transactional(rollbackFor = Exception.class)
    public VersionParametres updateParams(VpDonneeParamUpdateDto vpDonneeParamUpdateDto) throws NotFoundException, ConcurrentEditionException, SQLException, ValidationException {
        VersionParametres vpoSource = versionParametresRepository.findById(vpDonneeParamUpdateDto.getVpId()).orElse(null);

        if (Objects.isNull(vpoSource)) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{vpDonneeParamUpdateDto.getVpId()}, messageSource));
        }

        if (!TYPE_DONNE_VAL.equals(vpoSource.getValeurVp().getCodTypeDonnee())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_MODIF_VALEURS_ONLY_VAL_ALLOWED, new Object[]{vpDonneeParamUpdateDto.getVpId()}, messageSource));
        }

        log.debug("updateParams :: updatedVp.getVersion() = [" + vpDonneeParamUpdateDto.getVersion() + "]");
        log.debug("updateParams :: vp.getDateMaj().toEpochSecond(ZoneOffset.UTC) = [" + vpoSource.getDateMaj().toEpochSecond(ZoneOffset.UTC) + "]");
        // Valider si la version de paramètres a été modifiée depuis la dernière lecture (optimistic locking)
        if (!vpDonneeParamUpdateDto.getVersion().equals(vpoSource.getDateMaj().toEpochSecond(ZoneOffset.UTC))) {
            throw new ConcurrentEditionException(ApiMessageFactory.getError(ApiMessageCode.VP_MODIFIED_BY_OTHER_USER, messageSource));
        }

        Clob clob = new javax.sql.rowset.serial.SerialClob(vpDonneeParamUpdateDto.getDonneeParam().toCharArray());
        ValeurVp vpo = ValeurVp.builder().donneesParametres(clob).indASupprimer(vpoSource.getValeurVp().getIndASupprimer()).codTypeDonnee(vpoSource.getValeurVp().getCodTypeDonnee()).build();
        vpo.setDonneesParametres(clob);

        vpo = valeurVpRepository.saveAndFlush(vpo);
        vpo.addVersionParametres(vpoSource);

        vpoSource.setValeurVp(vpo);
        versionParametresRepository.saveAndFlush(vpoSource);

        valeurVpRepository.saveAndFlush(vpo);
        return vpoSource;
    }

    @Transactional(rollbackFor = Exception.class)
    public VpMiseEnExploitationResponseDto mettreEnExploitation(VpMiseEnExploitationDto dto) throws NotFoundException, ValidationException, WebClientException {
        VersionParametres vp = versionParametresRepository.findByIdFetchAssoVp(dto.getVpId()).orElse(null);

        if (Objects.isNull(vp)) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{dto.getVpId()}, messageSource));
        }

        if (ARCHIVEE.equals(vp.getIndArchivee())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_MISE_EXPLOITATION_ARCHIVEE_NOT_ALLOWED, new Object[]{dto.getVpId()}, messageSource));
        }

        dto.setDateDebEffec(dto.getDateDebEffec().withMinute(1));
        // Afin de créer un intervalle valide d'au minimum 1 journée, on doit ajouter une journée à la date de fin effective
        dto.setDateFinEffec(dto.getDateFinEffec().plusDays(1).withMinute(0));

        if (!dto.getDateFinEffec().isAfter(dto.getDateDebEffec())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_EXPL_END_DATE_MUST_BE_GREATER_THAN_START_DATE,
                    new Object[]{dto.getDateFinEffec(), dto.getDateDebEffec()}, messageSource
            ));
        }

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        AssoVersionParametres assoVp;
        if (vp.getNoVersion() == 0) { // Version présentement en exploitation
            assoVp = createAssoVp(dto, vp, nowUtc);
            assoVp = assoVersionParametresRepository.saveAndFlush(assoVp);
        } else { // vp.getNoVersion() != 0 (version qui n'est PAS en exploitation)
            // Le numéro de version de paramètres source n'est pas égal à 0. Le système crée une copie de la version de paramètres.
            VersionParametres sourceVp = VersionParametres.builder()
                    .pointId(vp.getPointId()).codeModele(vp.getCodeModele()).annee(vp.getAnnee()).saison(vp.getSaison())
                    .noVersion(noVersionEnExploitation).dateEnregistrement(nowUtc).build();
            sourceVp.setDescription(vp.getDescription());
            sourceVp.setPasDeTemps(vp.getPasDeTemps());
            sourceVp.setVersionOrig(vp);
            sourceVp.setValeurVp(vp.getValeurVp());
            sourceVp.setIndArchivee(vp.getIndArchivee());

            assoVp = createAssoVp(dto, sourceVp, nowUtc);
            sourceVp.addAssoVersionParametres(assoVp);

            sourceVp = versionParametresRepository.saveAndFlush(sourceVp);
            AssoVersionParametres assoVersionParametres = sourceVp.getAssoVersionParametres().stream().findFirst().orElse(null);
            if (Objects.nonNull(assoVersionParametres)) {
                assoVersionParametres.setVersionParametres(sourceVp);
            }
            sourceVp = versionParametresRepository.saveAndFlush(sourceVp);
            assoVp = sourceVp.getAssoVersionParametres().stream().findFirst().orElse(null);
        }

        // Force selecting that VP from the DB such that the VP instance managed by JPA will have the latest values as the DB record (USAGER_MAJ, DATE_MAJ)
        assoVersionParametresRepository.refresh(assoVp);

        var versParam = vp.getPointModele().getPoint().getCodeRef() + " " + vp.getCodeModele() + " " + vp.getAnnee() + vp.getSaison() + vp.getNoVersion();
        var creerJournalisationResponse = new ApiResponse<>(versParam);
        try {
            if (Objects.nonNull(assoVp)) {
                creerJournalisation(vp, assoVp);
                creerJournalisationResponse.setStatus(ApiResponse.Status.SUCCESS);
            }
        } catch (ValidationException e) {
            var apiMessages = ApiMessageFactory.getWarning(ApiMessageCode.JOURNALISATION_MEE_VP_ERROR, new Object[]{versParam}, messageSource);
            creerJournalisationResponse.setStatus(ApiResponse.Status.FAILURE);
            creerJournalisationResponse.setMessages(apiMessages);
        }

        NotifNouvDonneesResponse calvp = envoyerNotificationCalVp(dto);

        return VpMiseEnExploitationResponseDto.builder()
                .assoVersionParametres(assoVp)
                .notifNouvDonneesResponse(calvp)
                .creerJournalisationResponse(creerJournalisationResponse)
                .ancienneVersion(vp.getNoVersion().toString())
                .build();
    }

    private AssoVersionParametres createAssoVp(VpMiseEnExploitationDto dto, VersionParametres sourceVp, LocalDateTime nowUtc) {
        AssoVersionParametres assoVp = AssoVersionParametres.builder().versionParametres(sourceVp)
                .dateEnrEffective(nowUtc).noteVp(dto.getNote()).build();

        assoVp.setDateDebEffective(dto.getDateDebEffec().toLocalDateTime());
        assoVp.setDateFinEffective(dto.getDateFinEffec().toLocalDateTime());
        return assoVp;
    }

    private void creerJournalisation(VersionParametres vp, AssoVersionParametres assoVp) throws ValidationException {
        var point = vp.getPointModele().getPoint();
        var journalisation = Journalisation.builder()
                .dateDebut(assoVp.getDateDebEffective().withMinute(0).withSecond(0))
                .dateFin(assoVp.getDateFinEffective().withMinute(0).withSecond(0).minusSeconds(1))
                .commentaire(assoVp.getNoteVp())
                .usagerPrenom(SecurityHelper.getUserDetails().getGivenName())
                .usagerNom(SecurityHelper.getUserDetails().getFamilyName())
                .cip(SecurityHelper.getClaimStr("CIP"))
                .action(actionJournalService.getByCode(ActionJournal.EActionJournalCode.NOUVELLE_VP))
                .manual(false)
                .point(point.getCodeRef())
                .model(vp.getCodeModele())
                .actionNom(point.getCodeRef() + " " + vp.getCodeModele() + " " + vp.getAnnee() + vp.getSaison() + vp.getNoVersion())
                .build();
        journalisationService.create(Collections.singletonList(journalisation));
    }

    private NotifNouvDonneesResponse envoyerNotificationCalVp(VpMiseEnExploitationDto dto) throws WebClientException {
        NotifNouvDonneesResponse calvp;
        try {
            calvp = notifNouvDonneesService.lancerNotifNouvDonnees("CALVP", "PDC.95.03").block();
        } catch (Exception e) {
            log.error("Erreur lors de lancement de notification nouvelles données", e);
            List<ApiMessage> apiMessages = ApiMessageFactory.getError(ApiMessageCode.VP_MISE_EXPLOITATION_NOTIF_NOUVEAUX_DONNEES_ERROR, new Object[]{dto.getVpId()}, messageSource);
            throw new WebClientException(apiMessages);
        }
        return calvp;
    }

    @Transactional(readOnly = true)
    public List<VersionParametres> searchDistinct(VpSearchCriteriaDto dto) {
        return new ArrayList<>(versionParametresRepository.searchDistinct(dto));
    }

    @Transactional(readOnly = true)
    public List<VersionParametres> search(VpSearchCriteriaDto dto) {
        return new ArrayList<>(versionParametresRepository.search(dto));
    }

    public void synchronize() {
        calculConfigService
                .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_CALENDRIER_VP_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class, new QueryParam("dateHreRef", formatDate(new Date())))
                .exceptionally(e -> {
                    log.error("Erreur lors de la synchronisation des calendriers VP : {}", e.getMessage());
                    return null;
                });
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.format(date);
    }
}
