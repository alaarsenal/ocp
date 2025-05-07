package ca.qc.hydro.epd.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dao.PrevisionDatesCalculEtMeteoDao;
import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour1ModelDto;
import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour2ModelDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PrevisionDatesCalculEtMeteoImpl implements PrevisionDatesCalculEtMeteoDao {

    private static final PrevisionBqMeteoHeureCalculRetour1DataMapper DATA_MAPPER_RETOUR_1 = new PrevisionBqMeteoHeureCalculRetour1DataMapper();
    private static final PrevisionBqMeteoHeureCalculRetour2DataMapper DATA_MAPPER_RETOUR_2 = new PrevisionBqMeteoHeureCalculRetour2DataMapper();

    private static final String QUERY_DATE_HEURE_CAL_RETERN_1_VALUE = """
            SELECT a.date_hre_calc AS date_hre_calc_ret1,
                   a.cod_typ_prev AS typ_prev_ret1,
                   a.date_prev_mete AS date_hre_prev_mete_ret1,
                   a.date_cor_mete AS date_hre_auto_cor_mete_ret1
            FROM   pdc802_calc_ct a
            WHERE  a.point_id         = :POINT_ID
            AND    a.cod_modele       = :CODE_MODELE
            AND    a.cod_fonc         = :CODE_FONCTION
            AND    a.cod_produit_prev = :CODE_PRODUIT
            AND    a.cod_statut_prev  = :COD_STATUT_PREV
            AND    a.date_hre_calc = (SELECT MAX(b.date_hre_calc)
                                      FROM   pdc802_calc_ct b
                                      WHERE  b.date_hre_calc <= :DATE_REFERENCE)""";
    private static final String QUERY_DATE_HEURE_CAL_RETERN_2_VALUE = """
            WITH max_date_calculs AS (
                SELECT date_hre_calc AS date_calcul
                FROM  (SELECT b.date_hre_calc
                       FROM   pdc802_calc_ct b
                       WHERE  b.cod_typ_prev    IN (:COD_TYP_PREV)
                       AND    b.cod_fonc         = :CODE_FONCTION
                       AND    b.cod_modele       = :CODE_MODELE
                       AND    b.cod_produit_prev = :CODE_PRODUIT
                       AND    b.point_id         = :POINT_ID
                       AND    b.cod_statut_prev  = :COD_STATUT_PREV
                       AND    b.date_hre_calc    < :date_hre_calc_ret1
                       ORDER BY b.date_hre_calc DESC
                       )
                WHERE  ROWNUM = 1
            )
            SELECT c.date_hre_calc AS date_hre_calc_ret2,
                    c.date_prev_mete  AS date_hre_prev_mete_ret2,
                    c.date_cor_mete   AS date_hre_auto_cor_mete_ret2,
                    c.cod_typ_prev
            FROM   pdc802_calc_ct c,max_date_calculs max_date_calcul
            WHERE  c.date_hre_calc    = max_date_calcul.date_calcul
            AND    c.point_id         = :POINT_ID
            AND    c.cod_modele       = :CODE_MODELE
            AND    c.cod_fonc         = :CODE_FONCTION
            AND    c.cod_produit_prev = :CODE_PRODUIT""";

    private static final String DATE_HRE_CALC_RET_1 = "date_hre_calc_ret1";
    private static final String TYP_PREV_RET_1 = "typ_prev_ret1";
    private static final String DATE_REFERENCE = "DATE_REFERENCE";
    private static final String COD_STATUT_PREV = "COD_STATUT_PREV";
    private static final String COD_TYP_PREV = "COD_TYP_PREV";
    private static final String POINT_ID = "POINT_ID";
    private static final String CODE_MODELE = "CODE_MODELE";
    private static final String CODE_FONCTION = "CODE_FONCTION";
    private static final String CODE_PRODUIT = "CODE_PRODUIT";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public PrevisionBqMeteoHeureCalculRetour1ModelDto findPrevisionPlusRecentePremierRetour(LocalDateTime dateRef, Long pointId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(DATE_REFERENCE, dateRef)
                .addValue(POINT_ID, pointId)
                .addValue(CODE_MODELE, "MM")
                .addValue(CODE_FONCTION, "PRAUTO_M")
                .addValue(CODE_PRODUIT, "P50")
                .addValue(COD_STATUT_PREV, "COMP");

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionBqMeteoHeureCalculRetour1ModelDto> resultat = jdbcTemplate.query(QUERY_DATE_HEURE_CAL_RETERN_1_VALUE, parameters, DATA_MAPPER_RETOUR_1);

        log.debug("Temps d'exécution findPrevisionPlusRecentePremierRetour : {} ms", stopWatch.getTime());

        return resultat.stream().findFirst().orElse(null);
    }


    @Override
    public PrevisionBqMeteoHeureCalculRetour2ModelDto findPrevisionDatehreEtAutoCalculDeuxiemeRetour(PrevisionBqMeteoHeureCalculRetour1ModelDto previsionCalculRte1, Long pointId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(DATE_HRE_CALC_RET_1, previsionCalculRte1.getDteHreCalcRet1())
                .addValue(POINT_ID, pointId)
                .addValue(CODE_MODELE, "MM")
                .addValue(CODE_FONCTION, "PRAUTO_M")
                .addValue(CODE_PRODUIT, "P50")
                .addValue(COD_STATUT_PREV, "COMP")
                .addValue(COD_TYP_PREV, Arrays.asList("CYCL", "PPCT"));

        StopWatch stopWatch = StopWatch.createStarted();
        PrevisionBqMeteoHeureCalculRetour2ModelDto privisionDteHreCalcRet2;
        try {
            privisionDteHreCalcRet2 = jdbcTemplate.queryForObject(QUERY_DATE_HEURE_CAL_RETERN_2_VALUE, parameters, DATA_MAPPER_RETOUR_2);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

        log.debug("Temps d'exécution findPrevisionDatehreEtAutoCalculDeuxiemeRetour : {} ms", stopWatch.getTime());

        return privisionDteHreCalcRet2;
    }

    public static class PrevisionBqMeteoHeureCalculRetour1DataMapper implements RowMapper<PrevisionBqMeteoHeureCalculRetour1ModelDto> {
        @Override
        public PrevisionBqMeteoHeureCalculRetour1ModelDto mapRow(ResultSet rs, int i) throws SQLException {
            rs.setFetchSize(200);

            PrevisionBqMeteoHeureCalculRetour1ModelDto dto = new PrevisionBqMeteoHeureCalculRetour1ModelDto();

            if (Objects.nonNull(rs.getTimestamp(DATE_HRE_CALC_RET_1))) {
                dto.setDteHreCalcRet1(rs.getTimestamp(DATE_HRE_CALC_RET_1).toLocalDateTime());
            }

            if (Objects.nonNull(rs.getString(TYP_PREV_RET_1))) {
                dto.setTypPrevRet1(rs.getString(TYP_PREV_RET_1));
            }

            if (Objects.nonNull(rs.getTimestamp("date_hre_auto_cor_mete_ret1"))) {
                dto.setDteHreAutoCorMeteRet1(rs.getTimestamp("date_hre_auto_cor_mete_ret1").toLocalDateTime());
            }
            if (Objects.nonNull(rs.getTimestamp("date_hre_prev_mete_ret1"))) {
                dto.setDteHrePrevMeteRet1(rs.getTimestamp("date_hre_prev_mete_ret1").toLocalDateTime());
            }

            return dto;
        }
    }

    public static class PrevisionBqMeteoHeureCalculRetour2DataMapper implements RowMapper<PrevisionBqMeteoHeureCalculRetour2ModelDto> {
        @Override
        public PrevisionBqMeteoHeureCalculRetour2ModelDto mapRow(ResultSet rs, int i) throws SQLException {
            rs.setFetchSize(200);

            PrevisionBqMeteoHeureCalculRetour2ModelDto dto = new PrevisionBqMeteoHeureCalculRetour2ModelDto();

            if (Objects.nonNull(rs.getTimestamp("date_hre_calc_ret2"))) {
                dto.setDateHreCalcRet2(rs.getTimestamp("date_hre_calc_ret2").toLocalDateTime());
            }

            if (Objects.nonNull(rs.getTimestamp("date_hre_prev_mete_ret2"))) {
                dto.setDateHrePrevMeteRet2(rs.getTimestamp("date_hre_prev_mete_ret2").toLocalDateTime());
            }
            if (Objects.nonNull(rs.getTimestamp("date_hre_auto_cor_mete_ret2"))) {
                dto.setDateHreAutoCorMeteRet2(rs.getTimestamp("date_hre_auto_cor_mete_ret2").toLocalDateTime());
            }

            return dto;
        }
    }

}
