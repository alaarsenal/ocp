package ca.qc.hydro.epd.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.ActionJournal;
import ca.qc.hydro.epd.domain.DonneesModele;
import ca.qc.hydro.epd.domain.Journalisation;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.PointModele;
import ca.qc.hydro.epd.domain.PointModeleId;
import ca.qc.hydro.epd.dto.CreateDonneesModeleResponseDto;
import ca.qc.hydro.epd.dto.DonneesModeleDto;
import ca.qc.hydro.epd.dto.DonneesModeleHistoryDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.DonneesModeleMapper;
import ca.qc.hydro.epd.repository.DonneesModeleRepository;
import ca.qc.hydro.epd.repository.PointModeleRepository;
import ca.qc.hydro.security.helper.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DonneesModeleService {

    private final MessageSource messageSource;
    private final DonneesModeleRepository donneesModeleRepository;
    private final PointModeleRepository pointModeleRepository;
    private final DonneesModeleMapper donneesModeleMapper;
    private final JournalisationService journalisationService;
    private final ActionJournalService actionJournalService;
    private final PointService pointService;

    @Value("${hq.pdcalcul.donnees-modele.corr.intervalle-maximum:100}")
    private long corrMaxDateRange;
    @Value("${hq.pdcalcul.donnees-modele.pond.intervalle-maximum:366}")
    private long pondMaxDateRange;
    @Value("${hq.pdcalcul.donnees-modele.default-values.pas-de-temps:24}")
    private int defaultPasDeTemps;

    public List<DonneesModele> getHistory(DonneesModeleHistoryDto dto) throws NotFoundException {
        Point point = pointService.getOneByCodeRef(dto.getCodeRefPoint());
        return donneesModeleRepository.findHistory(point.getId(), dto.getCodesModeles(), dto.getDateDebEffec().toLocalDateTime(),
                dto.getDateFinEffec().toLocalDateTime(), dto.getTypeDonnee()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateDonneesModeleResponseDto create(DonneesModeleDto dto) throws ValidationException, NotFoundException {
        dto.setDateDebEffec(dto.getDateDebEffec().withMinute(1));
        // Afin de créer un intervalle valide d'au minimum 1 journée, on doit ajouter une journée à la date de fin effective
        dto.setDateFinEffec(dto.getDateFinEffec().plusDays(1));

        var result = createRaw(dto);

        var creerJournalisationResponse = new ApiResponse<>(dto.getCodeModele());
        try {
            creerJournalisation(dto);
            creerJournalisationResponse.setStatus(ApiResponse.Status.SUCCESS);
        } catch (ValidationException e) {
            ApiMessageCode journalisationError = DonneesModele.TypeDonnee.COR.getCode().equals(dto.getTypeDonnee()) ?
                    ApiMessageCode.JOURNALISATION_CORRECTION_ERROR :
                    ApiMessageCode.JOURNALISATION_PONDERATION_ERROR;
            var apiMessages = ApiMessageFactory.getWarning(journalisationError, new Object[]{dto.getCodeModele()}, messageSource);
            creerJournalisationResponse.setStatus(ApiResponse.Status.FAILURE);
            creerJournalisationResponse.setMessages(apiMessages);
        }

        return CreateDonneesModeleResponseDto.builder()
                .donneesModeles(result)
                .creerJournalisationResponse(creerJournalisationResponse)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<DonneesModele> createRaw(DonneesModeleDto dto) throws ValidationException, NotFoundException {
        validate(dto);

        List<DonneesModele> list = new ArrayList<>();
        DonneesModele donneesModele = createNewDonneesModele(dto.getCodeModele(), dto);
        list.add(donneesModele);

        // Seulement les corrections doivent être cascadées vers les modèles liés
        if (donneesModele.getTypeDonnee().equalsIgnoreCase(DonneesModele.TypeDonnee.COR.getCode())) {
            String modeleMin = donneesModele.getPointModele().getModele().getModeleMin();
            if (StringUtils.isNotBlank(modeleMin)) {
                list.add(createNewDonneesModele(modeleMin, dto));
            }
        }

        Iterable<DonneesModele> iterable = donneesModeleRepository.saveAll(list);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    private void creerJournalisation(DonneesModeleDto dto) throws ValidationException {
        var journalisation = Journalisation.builder()
                .dateDebut(dto.getDateDebEffec().toLocalDateTime().withMinute(0).withSecond(0))
                .dateFin(dto.getDateFinEffec().toLocalDateTime().withMinute(0).withSecond(0).minusSeconds(1))
                .commentaire(dto.getNote())
                .usagerPrenom(SecurityHelper.getUserDetails().getGivenName())
                .usagerNom(SecurityHelper.getUserDetails().getFamilyName())
                .cip(SecurityHelper.getClaimStr("CIP"))
                .action(actionJournalService.getByCode(
                        DonneesModele.TypeDonnee.COR.getCode().equals(dto.getTypeDonnee()) ?
                                ActionJournal.EActionJournalCode.CORRECTION :
                                ActionJournal.EActionJournalCode.PONDERATION))
                .manual(false)
                .point(dto.getCodeRefPoint())
                .model(dto.getCodeModele())
                .build();
        journalisationService.create(Collections.singletonList(journalisation));
    }

    private DonneesModele createNewDonneesModele(String codeModele, DonneesModeleDto dto) throws ValidationException, NotFoundException {
        Point point = pointService.getOneByCodeRef(dto.getCodeRefPoint());
        dto.setPointId(point.getId());
        Optional<PointModele> optionalPointModele =
                pointModeleRepository.findById(PointModeleId.builder().pointId(dto.getPointId()).codeModele(codeModele).build());
        if (optionalPointModele.isEmpty()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_MODELE_NOT_FOUND,
                    new Object[]{dto.getCodeRefPoint(), codeModele}, messageSource
            ));
        }
        PointModele pointModele = optionalPointModele.get();

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        DonneesModele donnnesMod = donneesModeleMapper.toDonneesModele(dto);
        donnnesMod = donnnesMod.toBuilder().codeModele(codeModele).pointModele(pointModele).dateEnrEffective(nowUtc)
                .pasDeTemps(dto.getPasDeTemps() == null ? defaultPasDeTemps : dto.getPasDeTemps()).build();
        donnnesMod.setDateDebEffective(dto.getDateDebEffec().toLocalDateTime());
        donnnesMod.setDateFinEffective(dto.getDateFinEffec().toLocalDateTime());
        return donnnesMod;
    }

    private void validate(DonneesModeleDto dto) throws ValidationException {
        if (dto.getDateFinEffec().isBefore(dto.getDateDebEffec())) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_END_DATE_MUST_BE_GREATER_THAN_START_DATE,
                    new Object[]{dto.getDateDebEffec(), dto.getDateFinEffec()}, messageSource
            ));
        }

        Duration duration = Duration.between(dto.getDateFinEffec(), dto.getDateDebEffec());
        long days = Math.abs(duration.toDays());

        long maxDateRange = (dto.getTypeDonnee().equalsIgnoreCase(DonneesModele.TypeDonnee.COR.getCode()) ? corrMaxDateRange : pondMaxDateRange);
        if (days > maxDateRange) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.DON_MOD_DATE_RANGE_TOO_BIG, new Object[]{maxDateRange}, messageSource));
        }
    }
}
