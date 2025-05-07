package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.dao.EcartPrevisionDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.DatesPrevisionDto;
import ca.qc.hydro.epd.dto.PrevisionDonnesConsoReelDto;
import ca.qc.hydro.epd.dto.PrevisionDonnesDto;
import ca.qc.hydro.epd.dto.PrevisionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-13
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class EcartPrevisionService {

    private static final String PLUS_RECENT_CALCUL = "PlusRecentCalcul";
    private static final String PROJECTION_QUOTIDIEN = "ProjectionQuotidien";

    private final EcartPrevisionDao ecartPrevisionDao;
    private final ConsommationService consommationService;
    private final PointService pointService;

    public PrevisionDonnesDto getPrevisions(
            DatesPrevisionDto datesPrevision,
            Integer projection,
            String tempsPrevision,
            List<String> codesRefPoints,
            List<String> modeles,
            List<String> fonctions,
            List<String> codesProduitPrev,
            List<String> codesTypePrevision,
            String etiquette,
            String typePrevision
    ) {
        PrevisionDonnesDto result = new PrevisionDonnesDto();
        List<PrevisionDto> resultatPrevisionDt = new ArrayList<>();

        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();

        // Plus récent calcul
        if (typePrevision.equalsIgnoreCase(PLUS_RECENT_CALCUL)) {
            resultatPrevisionDt = ecartPrevisionDao.findPrevisionsDonnesPlusRecentes(
                    datesPrevision,
                    pointsIds,
                    modeles,
                    fonctions,
                    codesProduitPrev,
                    codesTypePrevision
            );
        } else if (typePrevision.equalsIgnoreCase(PROJECTION_QUOTIDIEN)) {
            resultatPrevisionDt = ecartPrevisionDao.findPrevisionsDonnesQuotidiennes(
                    datesPrevision,
                    pointsIds,
                    modeles,
                    fonctions,
                    codesProduitPrev,
                    codesTypePrevision,
                    projection,
                    tempsPrevision,
                    etiquette
            );
        }

        if (resultatPrevisionDt.isEmpty()) {
            return result;
        }

        LocalDateTime plusRecentDateCalcul = resultatPrevisionDt.stream()
                .map(PrevisionDto::getDateHreCalc)
                .max(Comparator.naturalOrder())
                .orElse(null);

        resultatPrevisionDt = resultatPrevisionDt.stream()
                .filter(previsionDto -> plusRecentDateCalcul.equals(previsionDto.getDateHreCalc()))
                .toList();

        result.setDateCalcul(plusRecentDateCalcul);
        result.setEcartPrevisions(resultatPrevisionDt);
        result.setTypeCalcul(resultatPrevisionDt.get(0).getTypeCalcul());

        return result;
    }

    public PrevisionDonnesConsoReelDto getConsommations(LocalDateTime dateDebut, LocalDateTime dateFin, List<String> codesRefPoints) {
        List<PrevisionDto> consommations = consommationService.getConsommations(dateDebut, dateFin, null, codesRefPoints);
        PrevisionDonnesConsoReelDto dtoResult = new PrevisionDonnesConsoReelDto();
        dtoResult.setEcartPrevisions(consommations);
        return dtoResult;
    }
}
