package ca.qc.hydro.epd.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.HistoCons;
import ca.qc.hydro.epd.domain.HistoConsId;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.domain.SourceDonnee;
import ca.qc.hydro.epd.domain.TypeCons;
import ca.qc.hydro.epd.domain.TypeConsId;
import ca.qc.hydro.epd.dto.HistoConsDto;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.HistoConsIdMapper;
import ca.qc.hydro.epd.repository.HistoConsRepository;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.repository.SourceDonneeRepository;
import ca.qc.hydro.epd.repository.TypeConsRepository;
import ca.qc.hydro.epd.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HistoConsService {

    private final MessageSource messageSource;

    private final HistoConsRepository histoConsRepository;
    private final PointRepository pointRepository;
    private final SourceDonneeRepository sourceDonneeRepository;
    private final TypeConsRepository typeConsRepository;

    private final HistoConsIdMapper histoConsIdMapper;

    @Transactional(rollbackFor = Exception.class)
    public HistoCons create(HistoConsDto dto) throws ValidationException, SQLException {
        HistoCons histoCons = buildHistoCons(dto);
        histoCons.setJourCons(histoCons.getJourCons().withMinute(1)); // Ajout d'une minute au moment de la consommation
        histoCons.setDateEnr(LocalDateTime.now(ZoneOffset.UTC).withNano(0)); // La date d'enregistrement prend la valeur de la date courante
        return histoConsRepository.save(histoCons);
    }

    private HistoCons buildHistoCons(HistoConsDto dto) throws ValidationException, SQLException {
        HistoConsId histoConsId = histoConsIdMapper.toHistoConsId(dto.getId());

        Point point = pointRepository.findByCodeRef(dto.getId().getCodeRefPoint()).orElseThrow(
                () -> new ValidationException(ApiMessageFactory.getError(ApiMessageCode.POINT_NOT_FOUND, new Object[]{histoConsId.getPointId()}, messageSource))
        );

        histoConsId.setPointId(point.getId());

        SourceDonnee sourceDonnee = sourceDonneeRepository.findById(histoConsId.getCodeSource()).orElseThrow(
                () -> new ValidationException(ApiMessageFactory.getError(ApiMessageCode.SRC_DONNEE_NOT_FOUND, new Object[]{histoConsId.getCodeSource()}, messageSource))
        );

        TypeCons typeCons = typeConsRepository.findById(
                TypeConsId.builder().typeCons(histoConsId.getTypeCons()).porteeCons(histoConsId.getPorteeCons()).build()).orElseThrow(
                () -> new ValidationException(ApiMessageFactory.getError(ApiMessageCode.TYPE_CONS_NOT_FOUND,
                        new Object[]{histoConsId.getTypeCons(), histoConsId.getPorteeCons()}, messageSource
                ))
        );

        HistoCons histoCons = new HistoCons(histoConsId);
        return histoCons.toBuilder().point(point).source(sourceDonnee).type(typeCons)
                .donneesCons(JsonUtils.getStringAsClob(dto.getDonneesCons())).note(dto.getNote()).build();
    }
}
