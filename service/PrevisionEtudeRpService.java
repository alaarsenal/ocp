package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.utils.Predicates.distinctByKey;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.DonneeMeteo;
import ca.qc.hydro.epd.domain.MeteoEtude;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.PrevisionEtude;
import ca.qc.hydro.epd.domain.PrevisionEtudeRp;
import ca.qc.hydro.epd.domain.Produit;
import ca.qc.hydro.epd.domain.ProduitId;
import ca.qc.hydro.epd.domain.VarMeteoEtude;
import ca.qc.hydro.epd.domain.VersionParametres;
import ca.qc.hydro.epd.domain.VpRpConfig;
import ca.qc.hydro.epd.dto.PrevisionEtudeDto;
import ca.qc.hydro.epd.dto.PrevisionEtudeUpdateDto;
import ca.qc.hydro.epd.dto.VersionParametresDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.VersionParametresMapper;
import ca.qc.hydro.epd.repository.DonneeMeteoRepository;
import ca.qc.hydro.epd.repository.MeteoEtudeRepository;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.repository.PrevisionEtudeRepository;
import ca.qc.hydro.epd.repository.PrevisionEtudeRpRepository;
import ca.qc.hydro.epd.repository.ProduitRepository;
import ca.qc.hydro.epd.repository.VersionParametresRepository;
import ca.qc.hydro.security.helper.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrevisionEtudeRpService {

    private final DonneeMeteoRepository donneeMeteoRepository;
    private final MeteoEtudeRepository meteoEtudeRepository;
    private final PrevisionEtudeRepository previsionEtudeRepository;
    private final PrevisionEtudeRpRepository previsionEtudeRpRepository;
    private final ProduitRepository produitRepository;
    private final PointRepository pointRepository;
    private final VersionParametresRepository versionParametresRepository;
    private final VersionParametresMapper versionParametresIdMapper;
    private final MessageSource messageSource;
    @Value("${hq.pdcalcul.prev-etude.meteo-etude.produit.code-prod}")
    private String meteoEtudeCodeProd;
    @Value("${hq.pdcalcul.prev-etude.meteo-etude.produit.code-src}")
    private String meteoEtudeCodeSrc;
    @Value("${hq.pdcalcul.prev-etude.var-meteo-etude.donnee-meteo.types-don}")
    private String[] typesDonMeteo;

    @Transactional(rollbackFor = Exception.class)
    public PrevisionEtudeRp create(PrevisionEtudeDto dto) throws ValidationException {
        validateInput(dto);
        Optional<PrevisionEtude> optional = previsionEtudeRepository.findByCode(dto.getCode());
        if (optional.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_ALREADY_EXISTS, messageSource));
        }

        PrevisionEtudeRp prevEtude = new PrevisionEtudeRp();
        buildPrevisionEtude(prevEtude, dto);
        MeteoEtude meteoEtude = buildAndSaveMeteoEtude(prevEtude);
        buildPrevisionEtudeRp(prevEtude, dto.getVpIdDtoList(), meteoEtude);

        prevEtude = previsionEtudeRpRepository.save(prevEtude);
        log.debug("create :: Nouvelle prévision d'étude créée [" + prevEtude.getNoPrevEtude() + "]");
        return prevEtude;
    }

    @Transactional(rollbackFor = Exception.class)
    public PrevisionEtudeRp update(PrevisionEtudeUpdateDto dto) throws ConcurrentEditionException, NotFoundException, ValidationException {
        validateInput(dto);

        Optional<PrevisionEtudeRp> optional = previsionEtudeRpRepository.findById(dto.getNoPrevEtude());
        if (!optional.isPresent()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_NOT_FOUND, messageSource));
        }
        PrevisionEtudeRp prevEtudeRp = optional.get();
        // Valider si le lot a été modifié depuis la dernière lecture (optimistic locking)
        if (!dto.getVersion().equals(prevEtudeRp.getDateMaj().toEpochSecond(ZoneOffset.UTC))) {
            throw new ConcurrentEditionException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_MODIFIED_BY_OTHER_USER, messageSource));
        }
        buildPrevisionEtude(prevEtudeRp, dto);

        List<VpRpConfig> vpRpConfigs = new ArrayList<>(prevEtudeRp.getVpRpConfigs());
        // DISCLAIMER - Pour l'instant (phase 1 MVP), il n'y aura qu'une seule météo d'étude commune à toutes les VP's associées au lot de prévision d'étude
        // C'est la raison pour laquelle on récupère le numéro (NO_METE_ETUD) à partir de la première VP
        MeteoEtude meteoEtude = vpRpConfigs.get(0).getMeteoEtude();
        meteoEtude = updateAndSaveMeteoEtude(meteoEtude);

        mergeVpRpConfigs(prevEtudeRp, dto, meteoEtude);
        prevEtudeRp = previsionEtudeRpRepository.save(prevEtudeRp);
        log.debug("update :: Prévision d'étude mise à jour [" + prevEtudeRp.getNoPrevEtude() + "]");
        return prevEtudeRp;
    }

    private void validateInput(PrevisionEtudeDto dto) throws ValidationException {
        if (dto.getDateDeb() == null) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_START_DATE_MISSING, null, messageSource));
        }
        dto.setDateDeb(dto.getDateDeb().withMinute(1));
        if (dto.isIndCalcParDates()) { // Période de calcul par dates
            if (dto.getDateFin() == null) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_END_DATE_MISSING, null, messageSource));
            }
            // Afin de créer un intervalle valide d'au minimum 1 journée, on doit ajouter une journée et une minute à la date de fin
            dto.setDateFin(dto.getDateFin().plusDays(1).withMinute(1));
            if (dto.getDateFin().compareTo(dto.getDateDeb()) <= 0) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_END_DATE_MUST_BE_GREATER_THAN_START_DATE,
                        new Object[]{dto.getDateDeb(), dto.getDateFin()}, messageSource
                ));
            }
        } else { // Période de calcul par nombre de jours
            if (dto.getNbJoursAvant() == null) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_NB_JRS_AVANT_MISSING, null, messageSource));
            }
            if (dto.getNbJoursApres() == null) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_NB_JRS_APRES_MISSING, null, messageSource));
            }
            dto.setNbJoursAvant(Math.abs(dto.getNbJoursAvant()));
            dto.setNbJoursApres(Math.abs(dto.getNbJoursApres()));
        }

        // Valider que la prévision d'étude possède au moins une VP associée
        if (CollectionUtils.isEmpty(dto.getVpIdDtoList())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_RP_VP_MISSING, null, messageSource));
        }
        // Valider que TOUS les points des VP associées sont identiques
        long distinctCodesRefPoints = dto.getVpIdDtoList().stream().filter(distinctByKey(VersionParametresDto::getCodeRefPoint)).count();
        if (distinctCodesRefPoints > 1) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_RP_VP_MULTIPLE_COD_POINT, null, messageSource));
        }
    }

    private void buildPrevisionEtude(PrevisionEtude prevEtude, PrevisionEtudeDto dto) {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        prevEtude.setNom(dto.getNom());
        prevEtude.setDescription(dto.getDescription());
        prevEtude.setProprietaire(SecurityHelper.getUsername());
        prevEtude.setIndPublic(dto.isIndPublic() ? 'O' : 'N');
        prevEtude.setDateDebEtude(dto.getDateDeb().toLocalDateTime());
        if (prevEtude.getNoPrevEtude() == null) {
            prevEtude.setCode(dto.getCode()); // Le code de prévision d'étude est mis à jour uniquement dans le cas d'une création
            prevEtude.setDateEnrEtude(nowUtc); // La date d'enregistrement est mise à jour uniquement dans le cas d'une création
        }
        if (dto.isIndCalcParDates()) { // Période de calcul par dates
            prevEtude.setJourRefVsJourCourant(null); // Overwrite the existing value with NULL (in case of an update)
            prevEtude.setNbJours(Duration.between(dto.getDateDeb(), dto.getDateFin()).toDays());
        } else { // Période de calcul par nombre de jours
            prevEtude.setJourRefVsJourCourant(dto.getNbJoursAvant());
            prevEtude.setNbJours((long) (dto.getNbJoursApres() + dto.getNbJoursAvant() + 1));
        }
    }

    private MeteoEtude buildAndSaveMeteoEtude(PrevisionEtude previsionEtude) throws ValidationException {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);

        Optional<Produit> optProduit = produitRepository.findById(ProduitId.builder().codeSource(meteoEtudeCodeSrc).codeProduit(meteoEtudeCodeProd).build());
        if (!optProduit.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PRODUIT_NOT_FOUND, new Object[]{meteoEtudeCodeSrc, meteoEtudeCodeProd}, messageSource));
        }
        Produit produit = optProduit.get();
        MeteoEtude meteoEtude = MeteoEtude.builder()
                .code(previsionEtude.getCode()).nom(previsionEtude.getCode()).description(previsionEtude.getCode()).proprietaire(previsionEtude.getProprietaire())
                .indPublic(previsionEtude.getIndPublic()).dateEnrEtude(nowUtc).dateRefIntrMeteo(nowUtc).produit(produit)
                .build();

        // Création des variations de météo d'étude
        List<DonneeMeteo> donneesMeteo = donneeMeteoRepository.findByTypes(Arrays.asList(typesDonMeteo));
        if (!CollectionUtils.isEmpty(donneesMeteo)) {
            for (DonneeMeteo d : donneesMeteo) {
                meteoEtude.addVariations(VarMeteoEtude.builder().donneeMeteo(d).build());
            }
        }

        meteoEtude = meteoEtudeRepository.save(meteoEtude);
        log.debug("createMeteoEtude :: Nouvelle météo d'étude créée [" + meteoEtude.getNoMeteoEtude() + "]");
        return meteoEtude;
    }

    private MeteoEtude updateAndSaveMeteoEtude(MeteoEtude meteoEtude) {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        meteoEtude.setDateEnrEtude(nowUtc);
        meteoEtude.setDateRefIntrMeteo(nowUtc);
        return meteoEtudeRepository.save(meteoEtude);
    }

    private void buildPrevisionEtudeRp(PrevisionEtudeRp prevEtudeRp, List<VersionParametresDto> vpIds, MeteoEtude meteoEtude) throws ValidationException {
        String codeRefPoint = vpIds.get(0).getCodeRefPoint();
        Optional<Point> optionalPoint = pointRepository.findByCodeRef(codeRefPoint);
        if (optionalPoint.isEmpty()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{codeRefPoint}, messageSource));
        }
        prevEtudeRp.setPoint(optionalPoint.get());

        for (VersionParametresDto vpId : vpIds) {
            addVpRpConfig(prevEtudeRp, versionParametresIdMapper.toVersionParametres(vpId), meteoEtude);
        }
    }

    /**
     * DISCLAIMER - Pour l'instant, le service permet seulement d'associer de nouvelles VPs au lot de prévision d'étude
     * (RP - point principal). C'est ce qui a été défini pour la livraison de la MVP.
     */
    private void mergeVpRpConfigs(PrevisionEtudeRp prevEtudeRp, PrevisionEtudeDto dto, MeteoEtude meteoEtude) throws ValidationException {
        for (VersionParametresDto vpIdDto : dto.getVpIdDtoList()) {
            VersionParametres vpUniqueId = versionParametresIdMapper.toVersionParametres(vpIdDto);
            if (!prevEtudeRp.contains(vpUniqueId)) {
                addVpRpConfig(prevEtudeRp, vpUniqueId, meteoEtude);
            }
        }
    }

    private void addVpRpConfig(PrevisionEtudeRp prevEtudeRp, VersionParametres vpUniqueId, MeteoEtude meteoEtude) throws ValidationException {
        Optional<VersionParametres> optVp = versionParametresRepository.findById(vpUniqueId.getVpId());
        if (!optVp.isPresent()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.VP_NOT_FOUND, new Object[]{vpUniqueId}, messageSource));
        }
        // DISCLAIMER - MVP
        SecureRandom secureRandom = new SecureRandom();
        String codeRpConfig = prevEtudeRp.getCode().substring(0, Math.min(prevEtudeRp.getCode().length(), 5)) + (secureRandom.nextInt(90000) + 10000);
        VpRpConfig vpRpConfig = VpRpConfig.builder().code(codeRpConfig).nom(codeRpConfig).description(codeRpConfig)
                .prevEtudeRp(prevEtudeRp).versionParametres(optVp.get()).meteoEtude(meteoEtude).dateRefIntrNonMeteo(LocalDateTime.now(ZoneOffset.UTC).withNano(0))
                .indProfilBaseCalendrier('O').indProfilSpecCalendrier('O').indCorrCalendrier('N').build();
        prevEtudeRp.addVpRpConfig(vpRpConfig);
    }

}
