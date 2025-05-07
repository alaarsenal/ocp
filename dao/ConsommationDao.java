package ca.qc.hydro.epd.dao;

import java.time.LocalDateTime;
import java.util.List;

import ca.qc.hydro.epd.dto.PrevisionDto;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-13
 */
public interface ConsommationDao {

    List<PrevisionDto> findHistoriqueConsommations(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds
    );

    List<PrevisionDto> findConsommationsRecentes(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds
    );
}
