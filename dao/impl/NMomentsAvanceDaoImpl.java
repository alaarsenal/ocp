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

import ca.qc.hydro.epd.dao.NMomentsAvanceDao;
import ca.qc.hydro.epd.dto.NMomentAvanceDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-05-19
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NMomentsAvanceDaoImpl implements NMomentsAvanceDao {

    private static final NMomentsPrevisionDataMapper PREVISION_DATA_MAPPER = new NMomentsPrevisionDataMapper();

    private static final String QUERY_PREVISION = """
            SELECT
                   TO_DATE(jt.horodate, 'YYYYMMDDHH24MI')  date_prevision,
                   a.jour_prevu + (jt.projec_hor / 60 / 24) date_projection,
                   jt.val as valeur
            FROM   pdc801_prev_ct a,
                   JSON_TABLE(don_prev_ct, '$'
                      COLUMNS NESTED PATH '$.valeurs[*]'
                         COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                                 projec_hor NUMBER(10)     PATH '$.projec_hor',
                                 val        VARCHAR2(30) PATH '$.val')
                   ) jt
            WHERE  a.cod_produit_prev = :cod_produit_prev
            AND    a.cod_fonc      = :cod_fonc
            AND    a.cod_modele    = :cod_modele
            AND    a.point_id       IN (:points_ids)
            AND    a.cod_typ_prev       IN (:codes_type_prevision)
            AND    a.jour_prevu > :date_debut
            AND    a.jour_prevu <=  :date_fin
            AND    (:projection_max IS NULL OR a.project_hor_mn <= :projection_max)
            AND    (:projection_min IS NULL OR a.project_hor_mx >= :projection_min)
            AND    jt.projec_hor IN (:projections)
            AND    TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') IN (:dates_prevision)""";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<NMomentAvanceDto> findPrevisionsNMomentsAvance(
            LocalDateTime dateDebut, LocalDateTime dateFin,
            String codeProduitPrev,
            List<Integer> projections,
            List<LocalDateTime> datesPrevision,
            List<Long> pointsIds,
            String codeFonction,
            String codeModele, List<String> codesTypePrevision
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut)
                .addValue("date_fin", dateFin)
                .addValue("points_ids", pointsIds)//["BQ"]
                .addValue("cod_produit_prev", codeProduitPrev)
                .addValue("cod_fonc", codeFonction)//"PRAUTO_M"
                .addValue("cod_modele", codeModele)//"MM"
                .addValue("projections", projections)
                .addValue("dates_prevision", datesPrevision)
                .addValue("projection_max", projections.stream().max(Integer::compareTo).orElse(null))
                .addValue("projection_min", projections.stream().min(Integer::compareTo).orElse(null))
                .addValue("codes_type_prevision", codesTypePrevision);

        StopWatch stopWatch = StopWatch.createStarted();

        List<NMomentAvanceDto> resultat = jdbcTemplate.query(QUERY_PREVISION, parameters, PREVISION_DATA_MAPPER);

        log.debug("Temps d'exécution findPrevisionsNMomentsAvance : {} ms", stopWatch.getTime());

        return resultat;
    }

    public static class NMomentsPrevisionDataMapper implements RowMapper<NMomentAvanceDto> {
        @Override
        public NMomentAvanceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            rs.setFetchSize(200);

            NMomentAvanceDto dto = new NMomentAvanceDto();

            if (Objects.nonNull(rs.getTimestamp("date_projection"))) {
                dto.setDateProjection(rs.getTimestamp("date_projection").toLocalDateTime());
            }
            if (Objects.nonNull(rs.getTimestamp("date_prevision"))) {
                dto.setDatePrevision(rs.getTimestamp("date_prevision").toLocalDateTime());
            }
            dto.setValeur(Double.valueOf(rs.getString("valeur")));

            return dto;
        }
    }

}
