package ca.qc.hydro.epd.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dao.PrevisionHoraireDao;
import ca.qc.hydro.epd.dto.PrevisionHoraireDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DAO pour les prévisions horaires
 *
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2021-12-06
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PrevisionHoraireDaoImpl implements PrevisionHoraireDao {

    public static final ReelDataMapper DATA_MAPPER_REEL = new ReelDataMapper();
    public static final PrevDataMapper DATA_MAPPER_PREV = new PrevDataMapper();

    public static final String QUERY_REEL = """    		
            WITH prevision_reel AS (
                  SELECT date_minut_cons a,
                         date_minut_cons + (59/1440) date_minut_cons,
                         cons_minute
                  FROM   pdc303_histo_cons_min
                  WHERE  point_id = :POINT_ID
                  AND    date_minut_cons > :DATE_DEBUT
                  AND    date_minut_cons < :DATE_FIN
            )
            SELECT c.date_prevue,
                   c.valeur_pointe_reel,
                   c.valeur_heure_reel,
                   CASE
                     WHEN b.cons_minute = c.valeur_pointe_reel
                     THEN b.date_minut_cons - (59/1440)
                   END minute_pointe_reel
            FROM  (SELECT TRUNC(a.date_minut_cons, 'HH') date_prevue,
                      MAX(a.cons_minute) valeur_pointe_reel,
                      MAX(CASE
                             WHEN TO_CHAR(a.date_minut_cons, 'MI') = '59'
                             THEN a.cons_minute
                          END
                      ) valeur_heure_reel
                 FROM   prevision_reel a
                 GROUP BY TRUNC(a.date_minut_cons, 'HH')
                ) c,
                   prevision_reel b
            WHERE  b.cons_minute = c.valeur_pointe_reel
            AND    TRUNC(b.date_minut_cons, 'HH') = TRUNC(c.date_prevue, 'HH')""";

    public static final String QUERY_PREV_WITH_PART = """
            WITH max_date_calculs AS (
                SELECT date_hre_calc AS date_calcul
                FROM  (SELECT b.date_hre_calc
                       FROM   pdc802_calc_ct b
                       WHERE  b.cod_typ_prev    IN (:COD_TYP_PREV_PLUS_RECENT_CALCUL)
                       AND    b.cod_fonc         = :CODE_FONCTION_PLUS_RECENT_CALCUL
                       AND    b.cod_modele       = :CODE_MODELE_PLUS_RECENT_CALCUL
                       AND    b.cod_produit_prev = :CODE_PRODUIT_PLUS_RECENT_CALCUL
                       AND    b.point_id         = :POINT_ID
                       AND    b.cod_statut_prev  = :COD_STATUT_PREV_PLUS_RECENT_CALCUL
                       AND    b.date_hre_calc    < :DATE_REFERENCE
                       ORDER BY b.date_hre_calc DESC
                       )
                WHERE  ROWNUM = 1
            ),
            base_dates AS (
                SELECT date_prevue,
                       CASE
                          WHEN TRUNC(:DATE_DEBUT, 'HH') + ( ROWNUM / 24 ) > CAST(SYS_EXTRACT_UTC(SYSTIMESTAMP) AS DATE)
                             THEN (SELECT date_calcul
                                   FROM   max_date_calculs
                                  )
                             ELSE
                                TRUNC(:DATE_DEBUT, 'HH') + ( ROWNUM / 24 ) - ( :PROJECTION / 1440 )
                       END date_calcul
                FROM  (SELECT TRUNC(:DATE_DEBUT, 'HH') + ( ROWNUM / 24 ) date_prevue
                       FROM  dual
                       CONNECT BY
                       ROWNUM <= :NOMBRE_HEURES)
            ),
            prev_ct AS (
                SELECT base_dates.date_prevue,
                       base_dates.date_calcul,
                       cod_produit_prev,
                       don_prev_ct
                FROM   pdc801_prev_ct,
                       base_dates
                WHERE  point_id      = :POINT_ID
                AND    cod_modele    = :CODE_MODELE
                AND    cod_fonc      = :CODE_FONCTION
                AND    date_hre_calc = base_dates.date_calcul
                AND    jour_prevu   >= TRUNC(:DATE_DEBUT, 'HH')
                AND    jour_prevu    < TRUNC(:DATE_DEBUT, 'HH') + (:NOMBRE_HEURES / 24)
            ),
            previsions AS (
                SELECT date_prevue,
                       date_calcul,
                       cod_produit_prev,
                       TO_NUMBER(jt.val,'999999999999D99999999999999999999','nls_numeric_characters=''.,''') AS val,
                       TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') horodate,
                       jt.projec_hor,
                       jt.pte_prevue,
                       TO_TIMESTAMP(jt.minute_pte, 'yyyy-mm-dd"T"hh24:mi:ss"Z"') minute_pte
                FROM   prev_ct,
                       JSON_TABLE(don_prev_ct, '$'
                          COLUMNS NESTED PATH '$.valeurs[*]'
                             COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                                     projec_hor NUMBER(10)     PATH '$.projec_hor',
                                     val        VARCHAR2(30)   PATH '$.val',
                                     pte_prevue NUMBER(10,2)     PATH '$.pte_prevue',
                                     minute_pte VARCHAR2(100)  PATH '$.minute_pte')
                       ) jt
                WHERE  TRUNC((TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') + 59 / 1440), 'HH') = TRUNC(date_prevue, 'HH')
            )""";

    public static final String QUERY_PREV_SELECT_PART = """
            SELECT DISTINCT
                   prevpnt.date_prevue,
                   prev.date_calcul,
                   prevpnt.valeur_pointe_prevu,
                   prevpnt.valeur_heure_prevu,
                   prev.horodate minute_pointe_prevu
            FROM  (SELECT date_prevue date_prevue,
                          MAX(CASE
                                 WHEN cod_produit_prev = :CODE_PRODUIT
                                 THEN val
                                 ELSE NULL
                              END
                          ) valeur_pointe_prevu,
                          MAX(CASE
                                 WHEN horodate = date_prevue
                                      AND cod_produit_prev = :CODE_PRODUIT
                                 THEN val
                                 ELSE NULL
                              END
                          ) valeur_heure_prevu
                   FROM  previsions
                   GROUP BY date_prevue
                  ) prevpnt,
                  previsions prev
            WHERE prev.val       = prevpnt.valeur_pointe_prevu
            AND   TRUNC((prev.horodate + 59 / 1440), 'HH') = TRUNC(prevpnt.date_prevue, 'HH')
            ORDER BY 1""";

    public static final String QUERY_PREV_PRECALCULE_SELECT_PART = """
            SELECT DISTINCT
                   prevpnt.date_prevue,
                   prev.date_calcul,
                   prev.pte_prevue valeur_pointe_prevu,
                   prevpnt.valeur_heure_prevu,
                   prev.minute_pte minute_pointe_prevu
            FROM  (SELECT date_prevue date_prevue,
                          MAX(CASE
                                 WHEN horodate = date_prevue
                                      AND cod_produit_prev = :CODE_PRODUIT
                                 THEN val
                                 ELSE NULL
                              END
                          ) valeur_heure_prevu
                   FROM  previsions
                   GROUP BY date_prevue
                  ) prevpnt,
                  previsions prev
            WHERE TRUNC((prev.horodate + 59 / 1440), 'HH') = TRUNC(prevpnt.date_prevue, 'HH')
            ORDER BY 1""";

    public static final String QUERY_PREV_AVEC_TOLERANCE_WITH_PART = """
            WITH prev_ct AS (
                      SELECT :date_prevue date_prevue,
                             date_hre_calc date_calcul,
                             cod_produit_prev,
                             don_prev_ct
                      FROM   pdc801_prev_ct
                      WHERE  point_id      = :POINT_ID
                      AND    cod_modele    = :CODE_MODELE
                      AND    cod_fonc      = :CODE_FONCTION
                      AND    cod_produit_prev = :CODE_PRODUIT
                      AND    date_hre_calc >= :min_date_calcul
                      AND    date_hre_calc <= :max_date_calcul
                      AND    jour_prevu   = :jour_prevu
            ),
            previsions AS (
                      SELECT :date_prevue date_prevue,
                             date_calcul,
                             cod_produit_prev,
                             TO_NUMBER(jt.val,'999999999999D99999999999999999999','nls_numeric_characters=''.,''') AS val,
                             TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') horodate,
                             jt.projec_hor,
                             jt.pte_prevue,
                             TO_TIMESTAMP(jt.minute_pte, 'yyyy-mm-dd"T"hh24:mi:ss"Z"') minute_pte
                      FROM   prev_ct,
                             JSON_TABLE(don_prev_ct, '$'
                                COLUMNS NESTED PATH '$.valeurs[*]'
                                   COLUMNS(horodate   VARCHAR2(20)   PATH '$.horodate',
                                           projec_hor NUMBER(10)     PATH '$.projec_hor',
                                           val        VARCHAR2(30)   PATH '$.val',
                                           pte_prevue NUMBER(10)     PATH '$.pte_prevue',
                                           minute_pte VARCHAR2(100)  PATH '$.minute_pte')
                             ) jt
                      WHERE  TRUNC((TO_DATE(jt.horodate, 'YYYYMMDDHH24MI') + 59 / 1440), 'HH') = TRUNC(:date_prevue, 'HH')
            )""";

    private static final String DATE_DEBUT = "DATE_DEBUT";
    private static final String DATE_REFERENCE = "DATE_REFERENCE";
    private static final String DATE_FIN = "DATE_FIN";
    private static final String POINT_ID = "POINT_ID";
    private static final String NOMBRE_HEURES = "NOMBRE_HEURES";
    private static final String CODE_MODELE = "CODE_MODELE";
    private static final String CODE_FONCTION = "CODE_FONCTION";
    private static final String CODE_PRODUIT = "CODE_PRODUIT";
    private static final String PROJECTION = "PROJECTION";
    private static final String CODE_MODELE_PLUS_RECENT_CALCUL = "CODE_MODELE_PLUS_RECENT_CALCUL";
    private static final String CODE_FONCTION_PLUS_RECENT_CALCUL = "CODE_FONCTION_PLUS_RECENT_CALCUL";
    private static final String CODE_PRODUIT_PLUS_RECENT_CALCUL = "CODE_PRODUIT_PLUS_RECENT_CALCUL";
    private static final String COD_STATUT_PREV_PLUS_RECENT_CALCUL = "COD_STATUT_PREV_PLUS_RECENT_CALCUL";
    private static final String COD_TYP_PREV_PLUS_RECENT_CALCUL = "COD_TYP_PREV_PLUS_RECENT_CALCUL";

    private static final String DATE_PREVUE = "date_prevue";
    private static final String VALEUR_POINTE_REEL = "valeur_pointe_reel";
    private static final String VALEUR_HEURE_REEL = "valeur_heure_reel";
    private static final String MINUTE_POINTE_REEL = "minute_pointe_reel";
    private static final String VALEUR_POINTE_PREVU = "valeur_pointe_prevu";
    private static final String VALEUR_HEURE_PREVU = "valeur_heure_prevu";
    private static final String MINUTE_POINTE_PREVU = "minute_pointe_prevu";
    private static final String DATE_CALCUL = "date_calcul";
    private static final String JOUR_PREVU = "jour_prevu";
    private static final String MIN_DATE_CALCUL = "min_date_calcul";
    private static final String MAX_DATE_CALCUL = "max_date_calcul";
    public static final String PRAUTO = "PRAUTO";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Retourne la consommation réelle par heure à partir de la dateRef
     *
     * @param dateRef      date de référence
     * @param nombreHeures nombre d'heures
     * @param pointId      ID du point
     * @return liste des PrevisionHoraireDto avec les attributs datePrevue, minutePointeReel, valeurPointeReel et
     * valeurHeureReel
     */
    @Override
    public List<PrevisionHoraireDto> findConsommationReellePeriode(LocalDateTime dateRef, int nombreHeures, Long pointId) {
        StopWatch stopWatch = StopWatch.createStarted();

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(DATE_DEBUT, dateRef)
                .addValue(DATE_FIN, dateRef.plusHours(nombreHeures))
                .addValue(POINT_ID, pointId);

        List<PrevisionHoraireDto> resultat = jdbcTemplate.query(QUERY_REEL, parameters, DATA_MAPPER_REEL);

        log.debug("Temps d'exécution findConsommationReellePeriode : {} ms", stopWatch.getTime());

        return resultat;
    }

    /**
     * Retourne la consommation prévue pour les 72 heures à partir de la dateRef
     *
     * @param dateReferenceAjuste date de référence = Jour Prévu (Exemple: 2021-12-14 05:01:00)
     * @param projection          le nombre de minutes à soustraire de la datePrevue pour trouver la bonne date de
     *                            calcul
     * @param nombreHeures        nombre d'heures
     * @param pointId             ID du point
     * @return liste des PrevisionHoraireDto avec les attributs datePrevue, minutePointePrevu, valeurPointePrevu et
     * valeurHeurePrevu
     */
    @Override
    public List<PrevisionHoraireDto> findPrevisionPeriode(LocalDateTime dateReferenceAjuste, LocalDateTime dateReference, Integer projection, int nombreHeures, Long pointId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(POINT_ID, pointId)
                .addValue(DATE_DEBUT, dateReferenceAjuste)
                .addValue(DATE_REFERENCE, dateReference)
                .addValue(NOMBRE_HEURES, nombreHeures)
                .addValue(CODE_MODELE, "M")
                .addValue(CODE_FONCTION, PRAUTO)
                .addValue(CODE_PRODUIT, "P50")
                .addValue(PROJECTION, projection)
                .addValue(CODE_MODELE_PLUS_RECENT_CALCUL, "M")
                .addValue(CODE_FONCTION_PLUS_RECENT_CALCUL, PRAUTO)
                .addValue(CODE_PRODUIT_PLUS_RECENT_CALCUL, "P50")
                .addValue(COD_STATUT_PREV_PLUS_RECENT_CALCUL, "COMP")
                .addValue(COD_TYP_PREV_PLUS_RECENT_CALCUL, Arrays.asList("CYCL", "PPCT"));

        StopWatch stopWatch = StopWatch.createStarted();

        // chercher les prévisions avec pointes précalculés
        List<PrevisionHoraireDto> resultat = jdbcTemplate.query(QUERY_PREV_WITH_PART + QUERY_PREV_PRECALCULE_SELECT_PART, parameters, DATA_MAPPER_PREV);

        // calculer les pointes pour les dates non précalculées
        getPrevisionsNonPrecalculees(dateReference, projection, pointId, resultat);

        log.debug("Temps d'exécution findPrevisionPeriode : {} ms", stopWatch.getTime());

        return resultat;
    }

    /**
     * Retourne l'indice NP pour les 72 heures à partir de la dateRef
     *
     * @param dateReferenceAjuste date de référence
     * @param projection          N'a pas d'effet dans ce cas, mais c'est nécessaire pour la requête
     * @param nombreHeures        nombre d'heures
     * @param pointId             ID du point
     * @return liste des PrevisionHoraireDto avec les attributs datePrevue et indiceNp
     */
    @Override
    public List<PrevisionHoraireDto> findIndiceNpPeriode(LocalDateTime dateReferenceAjuste, LocalDateTime dateReference, Integer projection, int nombreHeures, Long pointId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(POINT_ID, pointId)
                .addValue(DATE_DEBUT, dateReferenceAjuste)
                .addValue(DATE_REFERENCE, dateReference)
                .addValue(NOMBRE_HEURES, nombreHeures)
                .addValue(CODE_MODELE, "M")
                .addValue(CODE_FONCTION, "NPMODL")
                .addValue(CODE_PRODUIT, "P50")
                .addValue(PROJECTION, projection)
                .addValue(CODE_MODELE_PLUS_RECENT_CALCUL, "M")
                .addValue(CODE_FONCTION_PLUS_RECENT_CALCUL, PRAUTO)
                .addValue(CODE_PRODUIT_PLUS_RECENT_CALCUL, "P50")
                .addValue(COD_STATUT_PREV_PLUS_RECENT_CALCUL, "COMP")
                .addValue(COD_TYP_PREV_PLUS_RECENT_CALCUL, Arrays.asList("CYCL", "PPCT"));

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionHoraireDto> resultat = jdbcTemplate.query(QUERY_PREV_WITH_PART + QUERY_PREV_SELECT_PART, parameters, DATA_MAPPER_PREV);

        log.debug("Temps d'exécution findIndiceNpPeriode : {} ms", stopWatch.getTime());

        return resultat;
    }

    @Override
    public List<PrevisionHoraireDto> findPrevisionPourDatePrevueAvecTolerance(
            LocalDateTime datePrevue, LocalDateTime dateCalcul, LocalDateTime jourPrevu, Long pointId,
            int toleranceDateCalculPrecendent, int toleranceDateCalculSuivant
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(POINT_ID, pointId)
                .addValue(DATE_PREVUE, datePrevue)
                .addValue(DATE_CALCUL, dateCalcul)
                .addValue(JOUR_PREVU, jourPrevu)
                .addValue(CODE_MODELE, "M")
                .addValue(CODE_FONCTION, PRAUTO)
                .addValue(CODE_PRODUIT, "P50")
                .addValue(MIN_DATE_CALCUL, dateCalcul.minusMinutes(toleranceDateCalculPrecendent))
                .addValue(MAX_DATE_CALCUL, dateCalcul.plusMinutes(toleranceDateCalculSuivant));

        StopWatch stopWatch = StopWatch.createStarted();

        // chercher les prévisions avec pointes précalculés
        List<PrevisionHoraireDto> resultat = jdbcTemplate.query(QUERY_PREV_AVEC_TOLERANCE_WITH_PART + QUERY_PREV_PRECALCULE_SELECT_PART, parameters, DATA_MAPPER_PREV);

        // calculer les pointes pour les dates non précalculées
        getPrevisionsNonPrecalculeesAvecTolerence(datePrevue, dateCalcul, jourPrevu, pointId,
                dateCalcul.minusMinutes(toleranceDateCalculPrecendent), dateCalcul.plusMinutes(toleranceDateCalculSuivant), resultat
        );

        log.debug("Temps d'exécution findPrevisionPourDatePrevueAvecTolerance : {} ms", stopWatch.getTime());

        return resultat;
    }

    @Override
    public List<PrevisionHoraireDto> findIndiceNpPourDatePrevueAvecTolerance(
            LocalDateTime datePrevue, LocalDateTime dateCalcul, LocalDateTime jourPrevu, Long pointId,
            int toleranceDateCalculPrecendent, int toleranceDateCalculSuivant
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(POINT_ID, pointId)
                .addValue(DATE_PREVUE, datePrevue)
                .addValue(DATE_CALCUL, dateCalcul)
                .addValue(JOUR_PREVU, jourPrevu)
                .addValue(CODE_MODELE, "M")
                .addValue(CODE_FONCTION, "NPMODL")
                .addValue(CODE_PRODUIT, "P50")
                .addValue(MIN_DATE_CALCUL, dateCalcul.minusMinutes(toleranceDateCalculPrecendent))
                .addValue(MAX_DATE_CALCUL, dateCalcul.plusMinutes(toleranceDateCalculSuivant));

        StopWatch stopWatch = StopWatch.createStarted();

        List<PrevisionHoraireDto> resultat = jdbcTemplate.query(QUERY_PREV_AVEC_TOLERANCE_WITH_PART + QUERY_PREV_SELECT_PART, parameters, DATA_MAPPER_PREV);

        log.debug("Temps d'exécution findPrevisionPourDatePrevueAvecTolerance : {} ms", stopWatch.getTime());

        return resultat;
    }

    private void getPrevisionsNonPrecalculees(LocalDateTime dateReference, Integer projection, Long pointId, List<PrevisionHoraireDto> previsionsPrecalculees) {
        var firstPointePrecalculNull = previsionsPrecalculees.stream().filter(prev ->
                Objects.isNull(prev.getValeurPointePrevu()) && Objects.nonNull(prev.getValeurHeurePrevu())
        ).findFirst().orElse(null);

        if (Objects.nonNull(firstPointePrecalculNull)) {
            var lastPointePrecalculNull = previsionsPrecalculees.stream().filter(prev ->
                    Objects.isNull(prev.getValeurPointePrevu()) && Objects.nonNull(prev.getValeurHeurePrevu())
            ).reduce((first, second) -> second).orElse(null);

            assert lastPointePrecalculNull != null;
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue(POINT_ID, pointId)
                    .addValue(DATE_DEBUT, firstPointePrecalculNull.getDatePrevue().minusHours(1).withMinute(1))
                    .addValue(DATE_REFERENCE, dateReference)
                    .addValue(NOMBRE_HEURES, ChronoUnit.HOURS.between(firstPointePrecalculNull.getDatePrevue(), lastPointePrecalculNull.getDatePrevue()) + 1)
                    .addValue(CODE_MODELE, "MM")
                    .addValue(CODE_FONCTION, "PRAUTO_M")
                    .addValue(CODE_PRODUIT, "P50")
                    .addValue(PROJECTION, projection)
                    .addValue(CODE_MODELE_PLUS_RECENT_CALCUL, "M")
                    .addValue(CODE_FONCTION_PLUS_RECENT_CALCUL, PRAUTO)
                    .addValue(CODE_PRODUIT_PLUS_RECENT_CALCUL, "P50")
                    .addValue(COD_STATUT_PREV_PLUS_RECENT_CALCUL, "COMP")
                    .addValue(COD_TYP_PREV_PLUS_RECENT_CALCUL, Arrays.asList("CYCL", "PPCT"));

            List<PrevisionHoraireDto> previsionsNonPrecalculees = jdbcTemplate.query(QUERY_PREV_WITH_PART + QUERY_PREV_SELECT_PART, parameters, DATA_MAPPER_PREV);
            previsionsNonPrecalculees.forEach(prev -> {
                PrevisionHoraireDto prevPrecalc = previsionsPrecalculees.stream().filter(prev1 -> prev1.getDatePrevue().equals(prev.getDatePrevue())).findFirst().orElse(null);
                if (Objects.nonNull(prevPrecalc)) {
                    prevPrecalc.setValeurPointePrevu(prev.getValeurPointePrevu());
                    prevPrecalc.setMinutePointePrevu(prev.getMinutePointePrevu());
                }
            });
        }
    }

    private void getPrevisionsNonPrecalculeesAvecTolerence(LocalDateTime datePrevue, LocalDateTime dateCalcul, LocalDateTime jourPrevu, Long pointId, LocalDateTime minDate, LocalDateTime maxDate, List<PrevisionHoraireDto> previsionsPrecalculees) {
        var firstPointePrecalculNull = previsionsPrecalculees.stream().filter(prev ->
                Objects.isNull(prev.getValeurPointePrevu()) && Objects.nonNull(prev.getValeurHeurePrevu())
        ).findFirst().orElse(null);

        if (Objects.nonNull(firstPointePrecalculNull)) {
            var lastPointePrecalculNull = previsionsPrecalculees.stream().filter(prev ->
                    Objects.isNull(prev.getValeurPointePrevu()) && Objects.nonNull(prev.getValeurHeurePrevu())
            ).reduce((first, second) -> second).orElse(null);

            assert lastPointePrecalculNull != null;
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue(POINT_ID, pointId)
                    .addValue(DATE_PREVUE, datePrevue)
                    .addValue(DATE_CALCUL, dateCalcul)
                    .addValue(JOUR_PREVU, jourPrevu)
                    .addValue(CODE_MODELE, "MM")
                    .addValue(CODE_FONCTION, "PRAUTO_M")
                    .addValue(CODE_PRODUIT, "P50")
                    .addValue(MIN_DATE_CALCUL, minDate)
                    .addValue(MAX_DATE_CALCUL, maxDate);

            List<PrevisionHoraireDto> previsionsNonPrecalculees = jdbcTemplate.query(QUERY_PREV_AVEC_TOLERANCE_WITH_PART + QUERY_PREV_SELECT_PART, parameters, DATA_MAPPER_PREV);
            previsionsNonPrecalculees.forEach(prev -> {
                PrevisionHoraireDto prevPrecalc = previsionsPrecalculees.stream().filter(prev1 -> prev1.getDatePrevue().equals(prev.getDatePrevue())).findFirst().orElse(null);
                if (Objects.nonNull(prevPrecalc)) {
                    prevPrecalc.setValeurPointePrevu(prev.getValeurPointePrevu());
                    prevPrecalc.setMinutePointePrevu(prev.getMinutePointePrevu());
                }
            });
        }
    }

    /**
     * Permet de mapper les résultats de la requête de la consommation réelle aux attributs du DTO correspondants
     */
    public static final class ReelDataMapper implements RowMapper<PrevisionHoraireDto> {
        @Override
        public PrevisionHoraireDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            rs.setFetchSize(200);

            PrevisionHoraireDto dto = new PrevisionHoraireDto();

            dto.setDatePrevue(rs.getTimestamp(DATE_PREVUE).toLocalDateTime());

            dto.setValeurPointeReel(rs.getDouble(VALEUR_POINTE_REEL));
            if (rs.wasNull()) {
                dto.setValeurPointeReel(null);
            }
            dto.setValeurHeureReel(rs.getDouble(VALEUR_HEURE_REEL));
            if (rs.wasNull()) {
                dto.setValeurHeureReel(null);
            }
            dto.setMinutePointeReel(rs.getTimestamp(MINUTE_POINTE_REEL).toLocalDateTime());

            return dto;
        }
    }

    /**
     * Permet de mapper les résultats de la requête de la consommation prévue aux attributs du DTO correspondants Dans
     * le cas d'indiceNp, la valeur correspondante est valeur_pointe_prevu avec CODE_MODELE = M et CODE_FONCTION =
     * NPMODL
     */
    public static final class PrevDataMapper implements RowMapper<PrevisionHoraireDto> {
        @Override
        public PrevisionHoraireDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            rs.setFetchSize(200);

            PrevisionHoraireDto dto = new PrevisionHoraireDto();

            dto.setDatePrevue(rs.getTimestamp(DATE_PREVUE).toLocalDateTime());

            dto.setValeurPointePrevu(rs.getDouble(VALEUR_POINTE_PREVU));
            if (rs.wasNull()) {
                dto.setValeurPointePrevu(null);
            }
            dto.setValeurHeurePrevu(rs.getDouble(VALEUR_HEURE_PREVU));
            if (rs.wasNull()) {
                dto.setValeurHeurePrevu(null);
            }
            if (Objects.nonNull(rs.getTimestamp(MINUTE_POINTE_PREVU))) {
                dto.setMinutePointePrevu(rs.getTimestamp(MINUTE_POINTE_PREVU).toLocalDateTime());
            }
            dto.setDateCalcul(rs.getTimestamp(DATE_CALCUL).toLocalDateTime());

            return dto;
        }
    }

}
