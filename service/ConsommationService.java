package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import ca.qc.hydro.epd.dao.ConsommationDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.PrevisionDto;
import ca.qc.hydro.epd.repository.PointRepository;
import ca.qc.hydro.epd.utils.DatePrevisionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-27
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ConsommationService {

    private final ConsommationDao consommationDao;
    private final PointRepository pointRepository;

    @Value("${hq.pdcalcul.consMinNbJoursAvantHistorisation:7}")
    private long consMinNbJoursAvantHistorisation;

    public List<PrevisionDto> getConsommations(
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            List<Integer> minutesPrevision,
            List<String> codesRefPoints
    ) {
        List<PrevisionDto> result = new ArrayList<>();

        List<Long> pointsIds = pointRepository.findAllByCodeRef(codesRefPoints).stream().map(Point::getId).toList();

        LocalDateTime jourCourant = LocalDateTime.now().atZone(ZoneId.of(DatePrevisionUtil.MONTREAL))
                .truncatedTo(ChronoUnit.DAYS)
                .withZoneSameInstant(ZoneId.of(DatePrevisionUtil.UTC))
                .toLocalDateTime();

        LocalDateTime dateHisto = jourCourant.minusDays(consMinNbJoursAvantHistorisation);

        // On utilise la table PDC302 pour les données historiques
        if (dateDebut.isBefore(dateHisto)) {
            LocalDateTime nouvelleDateFin = dateFin.isBefore(dateHisto) ? dateFin : dateHisto;
            result.addAll(consommationDao.findHistoriqueConsommations(
                    dateDebut,
                    nouvelleDateFin,
                    CollectionUtils.isEmpty(minutesPrevision) ?
                            DatePrevisionUtil.getDatesPrevision(dateDebut, nouvelleDateFin) :
                            DatePrevisionUtil.getDatesPrevision(dateDebut, minutesPrevision),
                    pointsIds
            ));
        }

        // On utilise la table PDC303 pour les dates les plus récentes
        if (dateFin.isAfter(dateHisto)) {
            LocalDateTime nouvelleDateDebut = dateDebut.isAfter(dateHisto) ? dateDebut : dateHisto;
            result.addAll(consommationDao.findConsommationsRecentes(
                    nouvelleDateDebut,
                    dateFin,
                    CollectionUtils.isEmpty(minutesPrevision) ?
                            DatePrevisionUtil.getDatesPrevision(nouvelleDateDebut, dateFin) :
                            DatePrevisionUtil.getDatesPrevision(nouvelleDateDebut, minutesPrevision),
                    pointsIds
            ));
        }

        return result;
    }

}
