package ca.qc.hydro.epd.dao;

import java.util.List;

import ca.qc.hydro.epd.dto.DatesPrevisionDto;
import ca.qc.hydro.epd.dto.PrevisionDto;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-13
 */
public interface EcartPrevisionDao {

    List<PrevisionDto> findPrevisionsDonnesPlusRecentes(
            DatesPrevisionDto datesPrevision,
            List<Long> pointsIds,
            List<String> modeles,
            List<String> fonctions,
            List<String> codesProduitPrev,
            List<String> codesTypePrevision
    );

    List<PrevisionDto> findPrevisionsDonnesQuotidiennes(
            DatesPrevisionDto datesPrevisionDto,
            List<Long> pointsIds,
            List<String> modeles,
            List<String> fonctions,
            List<String> codesProduitPrev,
            List<String> codesTypePrevision,
            Integer projection,
            String tempsPrevision,
            String etiquette
    );
}
