package ca.qc.hydro.epd.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dao.ConsommationDao;
import ca.qc.hydro.epd.dto.PrevisionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-13
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ConsommationDaoImpl implements ConsommationDao {

    public static final ConsommationDataMapper CONSOMMATION_DATA_MAPPER = new ConsommationDataMapper();

    public static final String QUERY_CONSOMMATION_PDC302 = """
            SELECT
                   TO_DATE(jt.horodate, 'YYYYMMDDHH24MI')  date_prevision,
                   jt.val as valeur
            FROM PDC302_HISTO_CONS a,
                   JSON_TABLE(don_cons, '$'
                      COLUMNS NESTED PATH '$.valeurs[*]'
                      COLUMNS(horodate VARCHAR2(20) PATH '$.horodate',
                      val NUMBER(28, 20) PATH '$.val')) jt
            WHERE  a.date_enr = (SELECT MAX(b.date_enr)
                                 FROM   PDC302_HISTO_CONS b
                                 WHERE  b.JOUR_CONS = a.JOUR_CONS
                                 AND    b.COD_SRC = a.COD_SRC
                                 AND    b.POINT_ID = a.POINT_ID
                                 AND    b.TYPE_CONS = a.TYPE_CONS
                                 AND    b.PORTEE_CONS = a.PORTEE_CONS)
            AND    a.JOUR_CONS >= :date_debut
            AND    a.JOUR_CONS <= :date_fin
            AND    a.COD_SRC IN (:code_src)
            AND    a.TYPE_CONS IN (:type_cons)
            AND    a.POINT_ID IN (:points_ids)
            AND    a.PORTEE_CONS IN (:portee_cons)
            AND    TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') IN (:dates_prevision)""";

    public static final String QUERY_CONSOMMATION_PDC303 = """
            SELECT
                  DATE_MINUT_CONS date_prevision,
                  CONS_MINUTE as valeur
            FROM PDC303_HISTO_CONS_MIN
            WHERE POINT_ID IN (:points_ids)
                  AND DATE_MINUT_CONS >= :date_debut
                  AND DATE_MINUT_CONS <= :date_fin
                  AND DATE_MINUT_CONS IN (:dates_prevision)""";

    private final NamedParameterJdbcTemplate jdbcTemplate;


    @Override
    public List<PrevisionDto> findHistoriqueConsommations(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut)
                .addValue("date_fin", dateFin)
                .addValue("points_ids", pointsIds)
                .addValue("dates_prevision", datesPrevision)
                .addValue("code_src", Arrays.asList("CC", "DÉ", "PD"))
                .addValue("type_cons", Collections.singletonList("PSS"))
                .addValue("portee_cons", Collections.singletonList("1M"));

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionDto> resultat = jdbcTemplate.query(QUERY_CONSOMMATION_PDC302, parameters, CONSOMMATION_DATA_MAPPER);

        log.debug("Temps d'exécution findConsommationsNMomentsAvance : {} ms", stopWatch.getTime());

        return resultat;
    }

    @Override
    public List<PrevisionDto> findConsommationsRecentes(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut)
                .addValue("date_fin", dateFin)
                .addValue("points_ids", pointsIds)
                .addValue("dates_prevision", datesPrevision);

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionDto> resultat = jdbcTemplate.query(QUERY_CONSOMMATION_PDC303, parameters, CONSOMMATION_DATA_MAPPER);

        log.debug("Temps d'exécution findConsommationsNMomentsAvance : {} ms", stopWatch.getTime());

        return resultat;
    }

    public static class ConsommationDataMapper implements RowMapper<PrevisionDto> {
        @Override
        public PrevisionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            rs.setFetchSize(200);

            PrevisionDto dto = new PrevisionDto();

            if (Objects.nonNull(rs.getTimestamp("date_prevision"))) {
                dto.setDatePrevision(rs.getTimestamp("date_prevision").toLocalDateTime());
            }
            dto.setValeur(rs.getDouble("valeur"));

            return dto;
        }
    }
}
