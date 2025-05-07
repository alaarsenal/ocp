package ca.qc.hydro.epd.dao;

import java.time.LocalDateTime;
import java.util.List;

import ca.qc.hydro.epd.dto.PrevisionHoraireDto;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2021-12-06
 */
public interface PrevisionHoraireDao {

    List<PrevisionHoraireDto> findConsommationReellePeriode(LocalDateTime dateRef, int nombreHeures, Long pointId);

    List<PrevisionHoraireDto> findPrevisionPeriode(LocalDateTime dateReferenceAjuste, LocalDateTime dateReference, Integer projection, int nombreHeures, Long pointId);

    List<PrevisionHoraireDto> findIndiceNpPeriode(LocalDateTime dateReferenceAjuste, LocalDateTime dateReference, Integer projection, int nombreHeures, Long pointId);

    List<PrevisionHoraireDto> findPrevisionPourDatePrevueAvecTolerance(LocalDateTime datePrevue, LocalDateTime dateCalcul, LocalDateTime jourPrevu, Long pointId, int toleranceDateCalculPrecedent, int toleranceDateCalculSuivant);

    List<PrevisionHoraireDto> findIndiceNpPourDatePrevueAvecTolerance(LocalDateTime datePrevue, LocalDateTime dateCalcul, LocalDateTime jourPrevu, Long pointId, int toleranceDateCalculPrecedent, int toleranceDateCalculSuivant);

}
