package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
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

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.AssoProfilBase;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.ProfilBase;
import ca.qc.hydro.epd.domain.ProfilBaseId;
import ca.qc.hydro.epd.dto.AssoProfilBaseDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.AssoProfilBaseRepository;
import ca.qc.hydro.epd.repository.ProfilBaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProfilBaseService {

    private final MessageSource messageSource;
    private final AssoProfilBaseRepository assoProfilBaseRepository;
    private final ProfilBaseRepository profilBaseRepository;
    private final PointService pointService;
    private final @Qualifier("calculConfigService") HttpClientService calculConfigService;

    @Value("${hq.pdcalcul.asso-profil.intervalle-maximum:366}")
    private long maxDateRange;

    @Transactional(rollbackFor = Exception.class)
    public List<AssoProfilBase> addAsso(AssoProfilBaseDto dto) throws NotFoundException, ValidationException {
        validate(dto);

        Point point = pointService.getOneByCodeRef(dto.getCodeRefPoint());

        ProfilBaseId profilBaseId = ProfilBaseId.builder().pointId(point.getId()).codeModele(dto.getCodeModele()).noProfil(dto.getNoProfil()).build();
        ProfilBase profilBase = get(profilBaseId);

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        List<AssoProfilBase> associations = new ArrayList<>();
        associations.add(createAssoProfilBase(dto, profilBase, nowUtc, point));

        String modeleMin = profilBase.getPointModele().getModele().getModeleMin();
        if (StringUtils.isNotBlank(modeleMin)) {
            ProfilBaseId modeleMinProfilBaseId = ProfilBaseId.builder().pointId(point.getId()).codeModele(modeleMin).noProfil(dto.getNoProfil()).build();
            ProfilBase modeleMinProfilBase = get(modeleMinProfilBaseId);

            associations.add(createAssoProfilBase(dto, modeleMinProfilBase, nowUtc, point));
        }

        Iterable<AssoProfilBase> iterable = assoProfilBaseRepository.saveAll(associations);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    @Transactional(readOnly = true)
    public ProfilBase get(ProfilBaseId profilBaseId) throws NotFoundException {
        Optional<ProfilBase> optionalProfilBase = profilBaseRepository.findById(profilBaseId);
        if (!optionalProfilBase.isPresent()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.PROFIL_NOT_FOUND,
                    new Object[]{profilBaseId.getPointId(), profilBaseId.getCodeModele(), profilBaseId.getNoProfil()}, messageSource
            ));
        }
        return optionalProfilBase.get();
    }

    private void validate(AssoProfilBaseDto dto) throws ValidationException {
        ServiceValidationUtils.validateDateRange(dto.getDateDebEffec(), dto.getDateFinEffec(), maxDateRange, messageSource);
    }

    private AssoProfilBase createAssoProfilBase(AssoProfilBaseDto dto, ProfilBase profilBase, LocalDateTime nowUtc, Point point) {
        AssoProfilBase asso = AssoProfilBase.builder().pointId(point.getId()).codeModele(profilBase.getCodeModele())
                .noProfil(profilBase.getNoProfil()).dateEnrEffective(nowUtc).build();
        asso.setDateDebEffective(dto.getDateDebEffec().toLocalDateTime().withMinute(1));
        asso.setDateFinEffective(dto.getDateFinEffec().toLocalDateTime());
        asso.setProfilBase(profilBase);
        return asso;
    }
    public void synchronize() {
        calculConfigService
                .asyncHttpCall(HttpMethod.POST, APIConstant.SYNCHRO_PROFIL_ENDPOINT, null, Collections.emptyMap(), ApiResponse.class)
                .exceptionally(e -> {
                    log.error("Erreur lors de la synchronisation des profils : {}", e.getMessage());
                    return null;
                })
                .thenAccept(response -> log.info("Synchronisation des profils terminée"));
    }
}
