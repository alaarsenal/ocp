package ca.qc.hydro.epd.dao;

import java.time.LocalDateTime;
import java.util.List;

import ca.qc.hydro.epd.dto.ValeursMeteoDto;

public interface TemperatureDorvalDao {

    List<ValeursMeteoDto> findTemperaturesMeteoPontDorval(
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            LocalDateTime dteHrePrevMete,
            LocalDateTime dteHreAutoCorMete
    );
}
