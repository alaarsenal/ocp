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

import ca.qc.hydro.epd.dao.EcartPrevisionDao;
import ca.qc.hydro.epd.dto.DatesPrevisionDto;
import ca.qc.hydro.epd.dto.PrevisionDto;
import ca.qc.hydro.epd.enums.ETypeCalcul;

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
public class EcartPrevisionDaoImpl implements EcartPrevisionDao {

    public static final PrevisionDataMapper PREVISION_DATA_MAPPER = new PrevisionDataMapper();


    public static final String QUERY_PLUS_RECENT = """
            SELECT TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') date_prevision,
                   TO_NUMBER(jt.val,'999999999999D99999999999999999999','nls_numeric_characters=''.,''') AS valeur,
                   a.date_hre_calc AS dateCalc,
                   a.cod_typ_prev AS typeCalc
            FROM   pdc801_prev_ct a,
                   JSON_TABLE(a.don_prev_ct, '$'
                      COLUMNS NESTED PATH '$.valeurs[*]'
                      COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                              projec_hor NUMBER(10)     PATH '$.projec_hor',
                              val        VARCHAR2(30) PATH '$.val')
                   ) jt
            WHERE  a.point_id IN (:points_ids)
            AND    a.jour_prevu       >= :date_debut
            AND    a.jour_prevu       <  :date_fin
            AND    a.cod_statut_prev  = 'COMP'
            AND    a.cod_modele       IN (:modeles)
            AND    a.cod_fonc         IN (:fonctions)
            AND    a.cod_produit_prev IN (:codes_produit_prev)
            AND    a.cod_typ_prev     IN (:codes_type_prevision)
            AND    a.date_hre_calc    = (SELECT MAX(b.date_hre_calc)
                                        FROM   pdc801_prev_ct b
                                        WHERE  b.point_id          = a.point_id
                                        AND    b.jour_prevu       = a.jour_prevu
                                        AND    b.cod_modele       = a.cod_modele
                                        AND    b.cod_fonc         = a.cod_fonc
                                        AND    b.cod_produit_prev = a.cod_produit_prev
                                        AND    b.cod_typ_prev     = a.cod_typ_prev
                                       )
            AND    SUBSTR(jt.horodate,11, 2) = '00'""";

    public static final String QUERY_QUOTIDIENNE = """
            SELECT TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') date_prevision,
                   TO_NUMBER(jt.val,'999999999999D99999999999999999999','nls_numeric_characters=''.,''') AS valeur,
                   a.date_hre_calc AS dateCalc,
                   a.cod_typ_prev AS typeCalc
            FROM   pdc801_prev_ct a,
                   JSON_TABLE(a.don_prev_ct, '$'
                              COLUMNS NESTED PATH '$.valeurs[*]'
                              COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                                      projec_hor NUMBER(10)     PATH '$.projec_hor',
                                      val        VARCHAR2(30)   PATH '$.val')
                   ) jt
            WHERE  a.point_id              IN (:points_ids)
            AND    a.jour_prevu           >= :date_debut
            AND    a.jour_prevu           < :date_fin
            AND    a.cod_statut_prev      = 'COMP'
            AND    TRUNC(a.date_hre_calc) = TRUNC(a.JOUR_PREVU) - :projection
            AND    a.cod_modele           IN (:modeles)
            AND    a.cod_fonc             IN (:fonctions)
            AND    a.cod_produit_prev     IN (:codes_produit_prev)
            AND    a.cod_typ_prev         IN (:codes_type_prevision)
            AND    (:etiquette IS NULL OR a.code_etiq = :etiquette)
            AND    TO_CHAR(a.date_hre_calc, 'HH24MI') = (SELECT MAX(TO_CHAR(b.date_hre_calc, 'HH24MI'))
                                                        FROM    pdc801_prev_ct b
                                                        WHERE   b.point_id = a.point_id
                                                        AND     b.jour_prevu = a.jour_prevu
                                                        AND     TRUNC(b.date_hre_calc) = TRUNC(b.jour_prevu) - :projection
                                                        AND     b.cod_modele           = a.cod_modele
                                                        AND     b.cod_fonc             = a.cod_fonc
                                                        AND     b.cod_produit_prev     = a.cod_produit_prev
                                                        AND     b.cod_typ_prev         = a.cod_typ_prev
                                                        AND     (:temps_prevision IS NULL OR TO_CHAR(b.date_hre_calc, 'HH24MI') <= :temps_prevision))""";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<PrevisionDto> findPrevisionsDonnesPlusRecentes(DatesPrevisionDto datesPrevision, List<Long> pointsIds, List<String> modeles, List<String> fonctions, List<String> codesProduitPrev, List<String> codesTypePrevision) {
        LocalDateTime dateDebut = datesPrevision.getDateDebut();
        LocalDateTime dateFin = datesPrevision.getDateFin();
        LocalDateTime dateReference = datesPrevision.getDateReference();

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut)
                .addValue("date_fin", dateFin)
                .addValue("date_reference", Objects.nonNull(dateReference) ? dateReference : LocalDateTime.now())
                .addValue("points_ids", pointsIds)
                .addValue("modeles", modeles)
                .addValue("fonctions", fonctions)
                .addValue("codes_produit_prev", codesProduitPrev)
                .addValue("codes_type_prevision", codesTypePrevision);

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionDto> result = jdbcTemplate.query(QUERY_PLUS_RECENT, parameters, PREVISION_DATA_MAPPER);


        log.debug("Temps d'exécution findPrevisionsDonnesPlusRecentes : {} ms", stopWatch.getTime());

        return result;
    }

    @Override
    public List<PrevisionDto> findPrevisionsDonnesQuotidiennes(DatesPrevisionDto datesPrevision, List<Long> pointsIds, List<String> modeles, List<String> fonctions, List<String> codesProduitPrev, List<String> codesTypePrevision, Integer projection, String tempsPrevision, String etiquette) {
        LocalDateTime dateDebut = datesPrevision.getDateDebut();
        LocalDateTime dateFin = datesPrevision.getDateFin();
        LocalDateTime dateReference = datesPrevision.getDateReference();

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date_debut", dateDebut)
                .addValue("date_fin", dateFin)
                .addValue("date_reference", Objects.nonNull(dateReference) ? dateReference : LocalDateTime.now())
                .addValue("points_ids", pointsIds)
                .addValue("modeles", modeles)
                .addValue("fonctions", fonctions)
                .addValue("codes_produit_prev", codesProduitPrev)
                .addValue("codes_type_prevision", codesTypePrevision)
                .addValue("projection", projection)
                .addValue("temps_prevision", tempsPrevision)
                .addValue("etiquette", etiquette);

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionDto> result = jdbcTemplate.query(QUERY_QUOTIDIENNE, parameters, PREVISION_DATA_MAPPER);

        log.debug("Temps d'exécution findPrevisionsDonnesQuotidiennes : {} ms", stopWatch.getTime());

        return result;
    }

    public static class PrevisionDataMapper implements RowMapper<PrevisionDto> {
        @Override
        public PrevisionDto mapRow(ResultSet rs, int i) throws SQLException {
            rs.setFetchSize(200);

            PrevisionDto dto = new PrevisionDto();

            if (Objects.nonNull(rs.getTimestamp("date_prevision"))) {
                dto.setDatePrevision(rs.getTimestamp("date_prevision").toLocalDateTime());
            }
            dto.setValeur(Double.valueOf(rs.getString("valeur")));

            if (Objects.nonNull(rs.getTimestamp("dateCalc"))) {
                dto.setDateHreCalc(rs.getTimestamp("dateCalc").toLocalDateTime());
            }

            dto.setTypeCalcul(ETypeCalcul.valueOf(rs.getString("typeCalc")));

            return dto;
        }
    }

}
