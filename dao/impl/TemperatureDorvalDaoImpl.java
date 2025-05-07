package ca.qc.hydro.epd.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dao.TemperatureDorvalDao;
import ca.qc.hydro.epd.dto.ValeursMeteoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TemperatureDorvalDaoImpl implements TemperatureDorvalDao {

    private static final ValeursTemperatureDataMapper DATA_MAPPER_TEMPERATURE = new ValeursTemperatureDataMapper();
    private static final String QUERY_TEMPERATURES_VALUES = """    		
            SELECT TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') horodate,
                   jt.val as valeur,
                   jt.*
            FROM   pdc504_m_meteo a,
                   JSON_TABLE(a.don_mmeteo, '$'
                      COLUMNS NESTED PATH '$.valeurs[*]'
                      COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                              val        NUMBER(28, 20) PATH '$.val')
                   ) jt
            WHERE  a.jour_prev >= :date_debut
            AND    a.jour_prev <= :date_fin
            AND    a.cod_point_mete  = 'CYUL'
            AND    a.type_don   = 'T'
            AND    a.portee_don = '1H'
            AND    a.niv_don = 0
            AND    a.date_gener = :dteHrePrevMete
            AND    a.date_enr = :dteHreAutoCorMete
            ORDER BY 1""";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<ValeursMeteoDto> findTemperaturesMeteoPontDorval(LocalDateTime dateDebut, LocalDateTime dateFin, LocalDateTime dteHrePrevMete, LocalDateTime dteHreAutoCorMete) {

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut.minusDays(2).toLocalDate().atStartOfDay())
                .addValue("date_fin", dateFin)
                .addValue("dteHrePrevMete", dteHrePrevMete)
                .addValue("dteHreAutoCorMete", dteHreAutoCorMete);

        StopWatch stopWatch = StopWatch.createStarted();
        List<ValeursMeteoDto> temperatures = jdbcTemplate.query(QUERY_TEMPERATURES_VALUES, parameters, DATA_MAPPER_TEMPERATURE);

        log.debug("Temps d'exécution findTemperaturesMeteoPontDorval : {} ms", stopWatch.getTime());

        return temperatures;
    }


    public static class ValeursTemperatureDataMapper implements RowMapper<ValeursMeteoDto> {
        @Override
        public ValeursMeteoDto mapRow(ResultSet rs, int i) throws SQLException {
            rs.setFetchSize(200);

            ValeursMeteoDto dto = new ValeursMeteoDto();

            if (Objects.nonNull(rs.getTimestamp("horodate"))) {
                dto.setHorodate(rs.getTimestamp("horodate").toLocalDateTime());
            }
            dto.setValeur(Float.valueOf(rs.getString("valeur")));

            return dto;
        }
    }

}
