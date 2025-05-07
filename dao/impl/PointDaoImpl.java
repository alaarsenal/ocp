package ca.qc.hydro.epd.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dao.PointDao;
import ca.qc.hydro.epd.dto.PointSearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PointDaoImpl implements PointDao {

    public static final PointDataMapper DATA_MAPPER_POINT = new PointDataMapper();

    private static final String SEARCH_QUERY = """                  
            WITH
            ordered_statuts AS (
              SELECT
                  POINT_ID,
                  COD_STATUT,
                  DTE_ENR,
                  ROW_NUMBER() OVER (PARTITION BY POINT_ID ORDER BY DTE_ENR DESC) AS rn
              FROM
                  PDC115_STATUT_POINT
            ),
            statuts AS (
              SELECT
                  POINT_ID,
                  COD_STATUT,
                  DTE_ENR
              FROM
                  ordered_statuts
              WHERE
                  rn = 1
            )
            SELECT
              pointId,
              codPoint,
              codRefPoint,
              nomPoint,
              descPoint,
              seuilEcartPrev,
              codTypPoint,
              codStatutCourant,
              codRegCourante,
              listagg(distinct ABREV_UNIT,', ' ) WITHIN GROUP (ORDER BY ABREV_UNIT) AS unites,
              groupement
            FROM (
              SELECT
                  p.pointId,
                  p.codPoint,
                  p.codRefPoint,
                  p.nomPoint,
                  p.descPoint,
                  p.seuilEcartPrev,
                  p.codTypPoint,
                  st.COD_STATUT AS codStatutCourant,
                  reg.COD_REG AS codRegCourante,
                  pm.ABREV_UNIT,
                  p.COD_GRP AS groupement
              FROM (
                  SELECT
                      pr.POINT_ID AS pointId,
                      pr.COD_POINT as codPoint,
                      pr.COD_REF_POINT AS codRefPoint,
                      pr.NOM_POINT AS nomPoint,
                      pr.DESC_POINT AS descPoint,
                      pr.SEUIL_ECART_PREV AS seuilEcartPrev,
                      pr.COD_TYP_POINT AS codTypPoint,
                      gr.COD_GRP
                  FROM
                      PDC101_POINTS pr,
                      PDC102_GRP_POINT gr,
                      PDC103_COMPOSANTE_GRP cr
                  WHERE
                      pr.POINT_ID  = cr.POINT_ID
                      AND cr.COD_GRP = gr.COD_GRP
              ) p
              LEFT JOIN PDC116_PRECISION_MESURE pm ON pm.POINT_ID = p.pointId
              LEFT JOIN PDC108_COMPOSANTE_REG cmp_reg ON cmp_reg.POINT_ID  = p.pointId
              LEFT JOIN PDC107_REGION reg ON reg.COD_REG = cmp_reg.COD_REG AND reg.COD_TYP_REG = cmp_reg.COD_TYP_REG\s
              LEFT JOIN statuts st ON st.POINT_ID = p.pointId
            )
            GROUP BY pointId, codPoint, codRefPoint, nomPoint, descPoint, seuilEcartPrev, codTypPoint, codStatutCourant, codRegCourante, groupement
            ORDER BY 3
            """;

    private static final String POINT_ID = "pointId";
    private static final String COD_POINT = "codPoint";
    private static final String COD_REF_POINT = "codRefPoint";
    private static final String NOM_POINT = "nomPoint";
    private static final String DESC_POINT = "descPoint";
    private static final String SEUIL_ECART_PREV = "seuilEcartPrev";
    private static final String COD_TYP_POINT = "codTypPoint";
    private static final String COD_STATUT_COURANT = "codStatutCourant";
    private static final String COD_REG_COURANTE = "codRegCourante";
    private static final String UNITES = "unites";
    private static final String GROUPEMENT = "groupement";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PointSearchDto> findPoints() {
        StopWatch stopWatch = StopWatch.createStarted();

        List<PointSearchDto> resultat = jdbcTemplate.query(SEARCH_QUERY, DATA_MAPPER_POINT);

        log.debug("Temps d'exécution findPoints : {} ms", stopWatch.getTime());

        return resultat;
    }

    public static final class PointDataMapper implements RowMapper<PointSearchDto> {
        @Override
        public PointSearchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            rs.setFetchSize(200);

            PointSearchDto dto = new PointSearchDto();
            dto.setPointId(rs.getLong(POINT_ID));
            dto.setCodPoint(rs.getString(COD_POINT));
            dto.setCodRefPoint(rs.getString(COD_REF_POINT));
            dto.setNomPoint(rs.getString(NOM_POINT));
            dto.setDescPoint(rs.getString(DESC_POINT));
            dto.setSeuilEcartPrev(rs.getBigDecimal(SEUIL_ECART_PREV));
            dto.setCodTypPoint(rs.getString(COD_TYP_POINT));
            dto.setCodStatutCourant(rs.getString(COD_STATUT_COURANT));
            dto.setCodRegCourante(rs.getString(COD_REG_COURANTE));
            dto.setUnites(rs.getString(UNITES));
            dto.setGroupement(rs.getString(GROUPEMENT));

            return dto;
        }
    }

}
