package ca.qc.hydro.epd.dao;

import java.time.LocalDateTime;

import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour1ModelDto;
import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour2ModelDto;

public interface PrevisionDatesCalculEtMeteoDao {

    PrevisionBqMeteoHeureCalculRetour1ModelDto findPrevisionPlusRecentePremierRetour(LocalDateTime dtHreRef, Long pointId);

    PrevisionBqMeteoHeureCalculRetour2ModelDto findPrevisionDatehreEtAutoCalculDeuxiemeRetour(PrevisionBqMeteoHeureCalculRetour1ModelDto previsionCalculRte1, Long pointId);

}
