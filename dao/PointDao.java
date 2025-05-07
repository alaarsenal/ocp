package ca.qc.hydro.epd.dao;

import java.util.List;

import ca.qc.hydro.epd.dto.PointSearchDto;

public interface PointDao {

    List<PointSearchDto> findPoints();

}
