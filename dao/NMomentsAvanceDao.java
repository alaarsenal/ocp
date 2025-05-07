package ca.qc.hydro.epd.dao;

import java.time.LocalDateTime;
import java.util.List;

import ca.qc.hydro.epd.dto.NMomentAvanceDto;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2021-12-06
 */
public interface NMomentsAvanceDao {

    List<NMomentAvanceDto> findPrevisionsNMomentsAvance(
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            String codeProduitPrev,
            List<Integer> projections,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds,
            String codeFonction,
            String codeModele,
            List<String> codesTypePrevision
    );

}
