package ca.qc.hydro.epd.dao.impl;

import ca.qc.hydro.epd.dao.PointPrevisionDao;
import ca.qc.hydro.epd.dto.PointPrevisionDto;
import ca.qc.hydro.epd.dto.PointSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PointPrevisionDaoImpl implements PointPrevisionDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String QUERY = """
            SELECT 
               p.COD_POINT, 
               p.COD_REF_POINT, 
               p.NOM_POINT, 
               r.COD_REG, 
               r.NOM_REG, 
               tp.COD_TYP_POINT, 
               tp.NOM_TYP_POINT
        FROM PDC101_POINTS p
        INNER JOIN PDC108_COMPOSANTE_REG cr ON p.POINT_ID = cr.POINT_ID
        INNER JOIN PDC107_REGION r ON cr.COD_REG = r.COD_REG
        INNER JOIN PDC106_TYP_POINT tp ON p.COD_TYP_POINT = tp.COD_TYP_POINT
            """;

    @Override
    public List<PointPrevisionDto> findAllPoints() {
        return jdbcTemplate.query(QUERY, new PointDataMapper());

    }

    public static final class PointDataMapper implements RowMapper<PointPrevisionDto> {
        @Override
        public PointPrevisionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointPrevisionDto dto = new PointPrevisionDto();
            dto.setCodPoint(rs.getString(1));
            dto.setCodRefPoint(rs.getString(2));
            dto.setNomPoint(rs.getString(3));
            dto.setCodReg(rs.getString(4));
            dto.setNomReg(rs.getString(5));
            dto.setCodTypPoint(rs.getString(6));
            dto.setNomTypPoint(rs.getString(7));
            return dto;
        }
    }
}
