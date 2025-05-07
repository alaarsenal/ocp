package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.dao.NMomentsAvanceDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.NMomentAvanceDto;
import ca.qc.hydro.epd.dto.PrevisionDto;
import ca.qc.hydro.epd.utils.DatePrevisionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-05-19
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NMomentsAvanceService {

    private final NMomentsAvanceDao nMomentsAvanceDao;
    private final ConsommationService consommationService;
    private final PointService pointService;

    public List<NMomentAvanceDto> getPrevisionsNMomentsAvance(
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            String codeProduitPrev,
            List<Integer> projections,
            List<Integer> minutesPrevision,
            List<String> codesRefPoints,
            String codeFonction,
            String codeModele,
            List<String> codesTypePrevision
    ) {
        List<LocalDateTime> datesPrevision = DatePrevisionUtil.getDatesPrevision(dateDebut, minutesPrevision);

        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();

        return nMomentsAvanceDao.findPrevisionsNMomentsAvance(dateDebut, dateFin,
                        codeProduitPrev,
                        projections,
                        datesPrevision,
                        pointsIds,
                        codeFonction,
                        codeModele,
                        codesTypePrevision
                )
                .stream()
                .sorted(Comparator.comparing(NMomentAvanceDto::getDatePrevision).thenComparing(NMomentAvanceDto::getDateProjection))
                .toList();
    }

    public List<PrevisionDto> getConsommationsNMomentsAvance(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            List<Integer> minutesPrevision,
            List<String> codesRefPoints
    ) {
        return consommationService.getConsommations(dateDebut, dateFin, minutesPrevision, codesRefPoints);
    }

}
