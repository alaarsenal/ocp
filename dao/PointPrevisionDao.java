package ca.qc.hydro.epd.dao;

import ca.qc.hydro.epd.dto.PointPrevisionDto;

import java.util.List;

public interface PointPrevisionDao {

    /**
    * Retourne la liste des codes points utilisés pour l'API prevision.
    * Chaque code point contient son type, region et nom
    *
    * @return Une {@link List} de {@link PointPrevisionDto} contenant la liste des codes points
    */
    List<PointPrevisionDto> findAllPoints();
}
