package ca.qc.hydro.epd.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.AssoProfilSpec;
import ca.qc.hydro.epd.domain.CoeffProfilSpec;
import ca.qc.hydro.epd.domain.CoeffProfilSpecId;
import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.PointModele;
import ca.qc.hydro.epd.domain.PointModeleId;
import ca.qc.hydro.epd.domain.ProfilSpec;
import ca.qc.hydro.epd.dto.AssoProfilSpecDto;
import ca.qc.hydro.epd.dto.CoeffProfilSpecCreateDto;
import ca.qc.hydro.epd.dto.CoeffProfilSpecUpdateDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.CoeffProfilSpecIdMapper;
import ca.qc.hydro.epd.mapper.CoeffProfilSpecMapper;
import ca.qc.hydro.epd.repository.AssoProfilSpecRepository;
import ca.qc.hydro.epd.repository.CoeffProfilSpecRepository;
import ca.qc.hydro.epd.repository.PointModeleRepository;
import ca.qc.hydro.epd.repository.ProfilSpecRepository;
import ca.qc.hydro.epd.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoeffProfilSpecService {

    private final MessageSource messageSource;
    private final CoeffProfilSpecMapper coeffProfilSpecMapper;
    private final CoeffProfilSpecIdMapper coeffProfilSpecIdMapper;
    private final AssoProfilSpecRepository assoProfilSpecRepository;
    private final CoeffProfilSpecRepository coeffProfilSpecRepository;
    private final ProfilSpecRepository profilSpecRepository;
    private final PointModeleRepository pointModeleRepository;
    private final PointService pointService;

    @Value("${hq.pdcalcul.asso-profil.intervalle-maximum:366}")
    private long maxDateRange;
    @Value("${hq.pdcalcul.coeff-profil-spec.default-values.pas-de-temps:24}")
    private int defaultPasDeTemps;

    @Transactional(rollbackFor = Exception.class)
    public List<AssoProfilSpec> addAsso(AssoProfilSpecDto dto) throws NotFoundException, ValidationException {
        validate(dto);

        var point = pointService.getOneByCodeRef(dto.getCodeRefPoint());

        CoeffProfilSpecId coeffProfilSpecId = CoeffProfilSpecId.builder().pointId(point.getId()).codeModele(dto.getCodeModele())
                .noProfil(dto.getNoProfil()).annee(dto.getAnnee()).build();
        CoeffProfilSpec coeffProfilSpec = get(coeffProfilSpecId);

        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        List<AssoProfilSpec> associations = new ArrayList<>();
        associations.add(createAssoProfilSpec(dto, coeffProfilSpec, nowUtc));

        String modeleMin = coeffProfilSpec.getPointModele().getModele().getModeleMin();
        if (Objects.nonNull(modeleMin) && !modeleMin.isBlank()) {
            CoeffProfilSpecId modeleMinCoeffProfilSpecId = CoeffProfilSpecId.builder().pointId(point.getId()).codeModele(modeleMin)
                    .noProfil(dto.getNoProfil()).annee(dto.getAnnee()).build();
            Optional<CoeffProfilSpec> optionalModeleMin = coeffProfilSpecRepository.findById(modeleMinCoeffProfilSpecId);
            if (optionalModeleMin.isEmpty()) {
                throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.COEFF_PROFIL_MODELE_MIN_NOT_FOUND, messageSource));
            }
            CoeffProfilSpec modeleMinCoeffProfilSpec = optionalModeleMin.get();
            associations.add(createAssoProfilSpec(dto, modeleMinCoeffProfilSpec, nowUtc));
        }

        Iterable<AssoProfilSpec> iterable = assoProfilSpecRepository.saveAll(associations);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CoeffProfilSpec> create(CoeffProfilSpecCreateDto dto) throws ValidationException, SQLException, NotFoundException {
        List<ProfilSpec> profilSpecToUpdate = new ArrayList<>();
        Optional<ProfilSpec> profilSpecOpt = profilSpecRepository.findById(dto.getId().getNoProfil());

        if (profilSpecOpt.isEmpty()) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PROFIL_SPEC_NOT_FOUND,
                    new Object[]{dto.getId().getNoProfil()}, messageSource
            ));
        }

        profilSpecToUpdate.add(profilSpecOpt.get());

        List<CoeffProfilSpec> toCreate = new ArrayList<>();
        CoeffProfilSpec coeffProfilSpec = buildCoeffProfil(dto);

        toCreate.add(coeffProfilSpec);

        String modeleMin = coeffProfilSpec.getPointModele().getModele().getModeleMin();
        if (Objects.nonNull(modeleMin) && !modeleMin.isBlank()) {
            // On remplace le modèle parent par le modèle lié/minute dans le DTO
            dto.getId().setCodeModele(modeleMin);
            toCreate.add(buildCoeffProfil(dto));
        }

        Iterable<CoeffProfilSpec> iterable = coeffProfilSpecRepository.saveAll(toCreate);
        profilSpecRepository.saveAll(profilSpecToUpdate);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    private CoeffProfilSpec buildCoeffProfil(CoeffProfilSpecCreateDto dto) throws ValidationException, SQLException, NotFoundException {
        CoeffProfilSpecId id = coeffProfilSpecIdMapper.toCoeffProfilSpecId(dto.getId());
        Optional<CoeffProfilSpec> coeffOptional = coeffProfilSpecRepository.findById(id);
        Integer pasDeTemps = dto.getPasDeTemps() != null ? dto.getPasDeTemps() : defaultPasDeTemps;

        CoeffProfilSpec coeffProfil;
        if (coeffOptional.isPresent()) {
            if (!dto.isOverwrite()) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COEFF_PROFIL_ALREADY_EXISTS, messageSource));
            } else {
                coeffProfil = coeffOptional.get();
                coeffProfil.setCoeffProfil(JsonUtils.getStringAsClob(dto.getCoeffProfil()));
                coeffProfil.setPasDeTemps(pasDeTemps);
            }
        } else {
            Point point = pointService.getOneByCodeRef(dto.getId().getCodeRefPoint());
            Optional<PointModele> optionalRePointMod =
                    pointModeleRepository.findById(PointModeleId.builder().pointId(point.getId()).codeModele(id.getCodeModele()).build());
            if (optionalRePointMod.isEmpty()) {
                throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_MODELE_NOT_FOUND,
                        new Object[]{id.getPointId(), id.getCodeModele()}, messageSource
                ));
            }
            PointModele pointModele = optionalRePointMod.get();
            coeffProfil = coeffProfilSpecMapper.createDtoToCoeffProfilSpec(dto);
            coeffProfil = coeffProfil.toBuilder().pointModele(pointModele).pasDeTemps(pasDeTemps).build();
        }
        return coeffProfil;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(List<CoeffProfilSpecId> ids) throws ValidationException, NotFoundException {
        List<CoeffProfilSpec> toDelete = new ArrayList<>();
        for (CoeffProfilSpecId id : ids) {
            // Le système valide qu'il existe une courbe de profil pour la combinaison point/modèle/profil/année demandée
            CoeffProfilSpec coeffProfilSpec = get(id);
            validateBeforeDelete(coeffProfilSpec);
            toDelete.add(coeffProfilSpec);

            // Si le modèle de cette courbe a un modèle lié (minute), il faut supprimer cette courbe aussi
            String modeleMin = coeffProfilSpec.getPointModele().getModele().getModeleMin();
            if (Objects.nonNull(modeleMin) && !modeleMin.isBlank()) {
                CoeffProfilSpecId modeleMinId = CoeffProfilSpecId.builder().pointId(id.getPointId()).codeModele(modeleMin)
                        .noProfil(id.getNoProfil()).annee(id.getAnnee()).build();
                Optional<CoeffProfilSpec> optModeleMin = coeffProfilSpecRepository.findById(modeleMinId);
                if (optModeleMin.isPresent()) {
                    validateBeforeDelete(optModeleMin.get());
                    toDelete.add(optModeleMin.get());
                }
            }
        }
        coeffProfilSpecRepository.deleteAll(toDelete);
    }

    private void validateBeforeDelete(CoeffProfilSpec coeffProfilSpec) throws ValidationException {
        // Le système valide qu'aucun profil n'est associé. Impossible de supprimer un profil qui est déjà associé au calendrier d’exécution.
        List<AssoProfilSpec> assos = assoProfilSpecRepository.findByPointAndModeleAndProfilAndAnnee(coeffProfilSpec.getPointId(), coeffProfilSpec.getCodeModele(),
                coeffProfilSpec.getNoProfil(), coeffProfilSpec.getAnnee()
        );
        if (!CollectionUtils.isEmpty(assos)) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PROFIL_ASSOCIE_CANNOT_BE_DELETED, messageSource));
        }
    }

    @Transactional(readOnly = true)
    public CoeffProfilSpec get(CoeffProfilSpecId coeffProfilSpecId) throws NotFoundException {
        Optional<CoeffProfilSpec> optionalCoeffProfilSpec = coeffProfilSpecRepository.findById(coeffProfilSpecId);
        if (!optionalCoeffProfilSpec.isPresent()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.COEFF_PROFIL_NOT_FOUND,
                    new Object[]{coeffProfilSpecId.getPointId(), coeffProfilSpecId.getCodeModele(), coeffProfilSpecId.getNoProfil(), coeffProfilSpecId.getAnnee()}, messageSource
            ));
        }
        return optionalCoeffProfilSpec.get();
    }

    @Transactional(readOnly = true)
    public List<Integer> getAnneesByPointsAndModelesAndProfils(List<String> codesRefPoints, List<String> codesMod, List<Integer> profils) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return coeffProfilSpecRepository.findAnneesByPointsAndModelesAndProfils(pointsIds, codesMod, profils);
    }

    @Transactional(readOnly = true)
    public List<Modele> getModelesByPoints(List<String> codesRefPoints) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return coeffProfilSpecRepository.findModelesByPoints(pointsIds);
    }

    @Transactional(readOnly = true)
    public List<ProfilSpec> getProfilsByPointsAndModeles(List<String> codesRefPoints, List<String> codesMod) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return coeffProfilSpecRepository.findProfilsByPointsAndModeles(pointsIds, codesMod);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CoeffProfilSpec> update(CoeffProfilSpecUpdateDto updateDto) throws NotFoundException, ConcurrentEditionException, SQLException {
        return update(updateDto, true, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CoeffProfilSpec> overwrite(CoeffProfilSpecUpdateDto updateDto) throws NotFoundException, ConcurrentEditionException, SQLException {
        // Temporairement désactiver la modification de la description pour éviter les erreurs du chargement en lot
        return update(updateDto, false, false);
    }

    private List<CoeffProfilSpec> update(CoeffProfilSpecUpdateDto updateDto, boolean validateVersion, boolean avecDescription) throws NotFoundException, ConcurrentEditionException, SQLException {
        List<CoeffProfilSpec> toUpdate = new ArrayList<>();
        List<ProfilSpec> profilSpecToUpdate = new ArrayList<>();
        CoeffProfilSpecId id = coeffProfilSpecIdMapper.toCoeffProfilSpecId(updateDto.getId());
        Optional<CoeffProfilSpec> coeffProfilOpt = coeffProfilSpecRepository.findById(id);
        if (coeffProfilOpt.isEmpty()) {
            throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.COEFF_PROFIL_NOT_FOUND, new Object[]{id}, messageSource));
        }
        CoeffProfilSpec coeffProfilSpec = coeffProfilOpt.get();
        // Valider si la courbe de profil a été modifiée depuis la dernière lecture (optimistic locking)
        if (validateVersion && !updateDto.getVersion().equals(coeffProfilSpec.getDateMaj().toEpochSecond(ZoneOffset.UTC))) {
            throw new ConcurrentEditionException(ApiMessageFactory.getError(ApiMessageCode.COEFF_MODIFIED_BY_OTHER_USER, messageSource));
        }
        coeffProfilSpec.setCoeffProfil(JsonUtils.getStringAsClob(updateDto.getCoeffProfil()));
        if (updateDto.getPasDeTemps() != null) {
            coeffProfilSpec.setPasDeTemps(updateDto.getPasDeTemps());
        }
        if (coeffProfilSpec.getProfilSpec() != null) {
            ProfilSpec modifieProfilSpec = coeffProfilSpec.getProfilSpec().toBuilder().build();

            if (avecDescription) {
                modifieProfilSpec.setDescription(updateDto.getDescription());
            }
            profilSpecToUpdate.add(modifieProfilSpec);
        }
        toUpdate.add(coeffProfilSpec);

        // Si le modèle de cette courbe a un modèle lié (minute), il faut mettre à jour cette courbe aussi
        String modeleMin = coeffProfilSpec.getPointModele().getModele().getModeleMin();
        if (Objects.nonNull(modeleMin) && !modeleMin.isBlank()) {
            CoeffProfilSpecId modeleMinId = CoeffProfilSpecId.builder().pointId(id.getPointId()).codeModele(modeleMin)
                    .noProfil(id.getNoProfil()).annee(id.getAnnee()).build();
            Optional<CoeffProfilSpec> optModeleMin = coeffProfilSpecRepository.findById(modeleMinId);
            CoeffProfilSpec coeffProfilModeleMin = optModeleMin.orElseGet(() -> CoeffProfilSpec.builder().pointId(id.getPointId()).codeModele(modeleMin)
                    .noProfil(id.getNoProfil()).annee(id.getAnnee()).pasDeTemps(defaultPasDeTemps).build());
            coeffProfilModeleMin.setCoeffProfil(JsonUtils.getStringAsClob(updateDto.getCoeffProfil()));
            if (updateDto.getPasDeTemps() != null) {
                coeffProfilModeleMin.setPasDeTemps(updateDto.getPasDeTemps());
            }
            toUpdate.add(coeffProfilModeleMin);
        }
        Iterable<CoeffProfilSpec> iterable = coeffProfilSpecRepository.saveAll(toUpdate);
        profilSpecRepository.saveAll(profilSpecToUpdate);
        return ImmutableList.copyOf(iterable); // Iterable to Collection
    }

    private void validate(AssoProfilSpecDto dto) throws ValidationException {
        ServiceValidationUtils.validateDateRange(dto.getDateDebEffec(), dto.getDateFinEffec(), maxDateRange, messageSource);
    }

    private AssoProfilSpec createAssoProfilSpec(AssoProfilSpecDto dto, CoeffProfilSpec coeffProfilSpec, LocalDateTime nowUtc) {
        AssoProfilSpec asso = AssoProfilSpec.builder().pointId(coeffProfilSpec.getPointId()).codeModele(coeffProfilSpec.getCodeModele())
                .noProfil(coeffProfilSpec.getNoProfil()).annee(coeffProfilSpec.getAnnee()).dateEnrEffective(nowUtc).build();
        asso.setDateDebEffective(dto.getDateDebEffec().toLocalDateTime().withMinute(1));
        asso.setDateFinEffective(dto.getDateFinEffec().toLocalDateTime());
        asso.setCoeffProfilSpec(coeffProfilSpec);
        return asso;
    }
}
