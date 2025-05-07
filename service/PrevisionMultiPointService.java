package ca.qc.hydro.epd.service;

import ca.qc.hydro.epd.domain.ConsbrutRedshift;
import ca.qc.hydro.epd.domain.PrevisionRedshift;
import ca.qc.hydro.epd.dto.PointPrevisionDto;
import ca.qc.hydro.epd.dto.ResultatPointsPrevisions;
import ca.qc.hydro.epd.dto.ResultatPointsPrevisions.PointPrevision;
import ca.qc.hydro.epd.exception.RedshiftQueryException;
import ca.qc.hydro.epd.redshift.RedshiftQueryExecutor;
import ca.qc.hydro.epd.utils.Constantes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.redshiftdata.model.SqlParameter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisionMultiPointService {

    @Value("${epd.aws.redshift.schema}")
    private String schema;
    private final PointService pointService;
    private final RedshiftQueryExecutor redshiftQueryExecutor;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String QUERY_ALL_POINTS = """
            SELECT * FROM %s.prevision p
            WHERE p.probabilite = 'P50'
            AND p.dateutc BETWEEN :dateDebut AND :dateFin
            """;
    public static final String QUERY_CONSOMMATION = """
            SELECT DISTINCT c.dateutc, c.minute, c.codept, c.cons FROM %s.consbrut c
            WHERE c.dateutc BETWEEN :dateDebut AND :dateFin
            AND EXTRACT(MINUTE FROM c.dateutc) = 0
            """;

    public ResultatPointsPrevisions getPointPrevisions(OffsetDateTime dateReference,
                                                       int nombreHeure) {
        LocalDateTime dateFin = dateReference.atZoneSameInstant(Constantes.ZONE_ID_UTC).toLocalDateTime();
        LocalDateTime dateDebut = dateFin.minusHours(nombreHeure);
        var dateFinParam = SqlParameter.builder()
                .name("dateFin")
                .value(dateFin.format(DATETIME_FORMATTER))
                .build();
        var dateDebutParam = SqlParameter.builder()
                .name("dateDebut")
                .value(dateDebut.format(DATETIME_FORMATTER))
                .build();

        var sqlParams = new ArrayList<>(List.of(dateDebutParam, dateFinParam));
        log.info("dateDebutParam: {}", dateDebutParam);
        log.info("dateFinParam: {}", dateFinParam);

        List<PointPrevisionDto> pointsPrev = pointService.getAllPointsForPrevision();
        log.info("pointsPrev: {}", pointsPrev.size());

        List<PrevisionRedshift> previsionRedshifts = getPrevisions(sqlParams);
        List<ConsbrutRedshift> consbrutRedshifts = getObservations(sqlParams);


        List<PointPrevision> pointPrevisions = previsionRedshifts.stream()
                .filter(prevision -> prevision.dateUtc().equals(dateFin))
                .map(prevision -> {
                    // Trouver le point correspondant dans la liste des points
                    var matchingPoint = pointsPrev.stream()
                            .filter(point -> point.getCodPoint().equals(prevision.codePoint()))
                            .findFirst()
                            .orElse(null);

                    // Construire un PointPrevision
                    return PointPrevision.builder()
                            .idCalc(prevision.idCalc())
                            .point(matchingPoint != null ? new ResultatPointsPrevisions.Point(
                                    matchingPoint.getCodPoint(),
                                    matchingPoint.getCodRefPoint(),
                                    matchingPoint.getNomPoint()
                            ) : null)
                            .region(matchingPoint != null ? new ResultatPointsPrevisions.Region(
                                    matchingPoint.getCodReg(),
                                    matchingPoint.getNomReg()
                            ) : null)
                            .type(matchingPoint != null ? new ResultatPointsPrevisions.Type(
                                    matchingPoint.getCodTypPoint(),
                                    matchingPoint.getNomTypPoint()
                            ) : null)
                            //.submitDate(prevision.submitDate().atOffset(ZoneOffset.UTC))
                            .submitDate(prevision.dateUtc().atOffset(ZoneOffset.UTC))
                            .build();
                })
                .toList();

        remplirEcartMapObservation(nombreHeure, dateFin.getHour(), pointPrevisions, previsionRedshifts, consbrutRedshifts);
        // Construire et retourner l'objet ResultatPointsPrevisions
        return ResultatPointsPrevisions.builder()
                .pointPrevisions(pointPrevisions)
                .build();
    }

    private List<PrevisionRedshift> getPrevisions(List<SqlParameter> sqlParams) {
        List<PrevisionRedshift> previsionRedshifts;
        try {
            previsionRedshifts = redshiftQueryExecutor.queryForList(String.format(QUERY_ALL_POINTS, schema), sqlParams,
                    result -> new PrevisionRedshift(
                            result.getFirst().stringValue(), // idCalc
                            LocalDateTime.parse(result.get(1).stringValue(), DATETIME_FORMATTER), // submitDate
                            result.get(2).stringValue(), // typeCalc
                            LocalDateTime.parse(result.get(3).stringValue(), DATETIME_FORMATTER), // dateUtc
                            result.get(6).stringValue(), // probabilite
                            result.get(7).stringValue(), // codePoint
                            result.get(9).doubleValue() // prevMw
                    )
            );
        }
        catch (RedshiftQueryException e) {
            throw new RuntimeException(e);
        }
        log.info("previsionRedshifts: {}", previsionRedshifts.size());
        return previsionRedshifts;
    }

    private List<ConsbrutRedshift> getObservations(List<SqlParameter> sqlParams) {//Consommation - Consbrut
        List<ConsbrutRedshift> consbrutRedshifts;
        try {
            consbrutRedshifts = redshiftQueryExecutor.queryForList(String.format(QUERY_CONSOMMATION, schema), sqlParams,
                    result -> new ConsbrutRedshift(
                            LocalDateTime.parse(result.getFirst().stringValue(), DATETIME_FORMATTER), // dateUtc
                            result.get(1).longValue().intValue(), // minute
                            result.get(2).stringValue(), // codePt
                            result.get(3).doubleValue() // cons
                    )
            );
        }
        catch (RedshiftQueryException e) {
            throw new RuntimeException(e);
        }
        log.info("consbrutRedshifts: {}", consbrutRedshifts.size());
        return consbrutRedshifts.stream().filter(consbrutRedshift -> consbrutRedshift.minute().toString().endsWith("00")).toList();
    }

    private void remplirEcartMapObservation(int nombreHeure, int t, List<PointPrevision> pointPrevisions,
            List<PrevisionRedshift> previsionRedshifts, List<ConsbrutRedshift> consbrutRedshifts) {
        pointPrevisions.forEach(pointPrevision -> {
            // Ecart
            var observations = consbrutRedshifts.stream().filter(consbrutRedshift ->
                    consbrutRedshift.codePt().equals(pointPrevision.getPoint().getCode())).toList();
            var previsions = previsionRedshifts.stream().filter(previsionRedshift ->
                    previsionRedshift.idCalc().equals(pointPrevision.getIdCalc())
                            && previsionRedshift.codePoint().equals(pointPrevision.getPoint().getCode())).toList();
            double totalEcart = 0.0;
            for (int k = 1; k <= nombreHeure; k++) {
                int index = t + 1 - k;

                var matchPrev = previsions.stream()
                        .filter(prevision -> prevision.dateUtc().getHour() == index)
                        .findFirst()
                        .orElse(null);
                var matchObsv = observations.stream()
                        .filter(observation -> observation.dateUtc().getHour() == index)
                        .findFirst()
                        .orElse(null);
                double prevision = matchPrev !=null && matchPrev.prevMw() != null ? matchPrev.prevMw().doubleValue() : 0.0;
                double observation = matchObsv !=null && matchObsv.cons() != null ? matchObsv.cons().doubleValue() : 0.0;
                totalEcart += (prevision - observation);
            }
            double ecartMoyen = totalEcart / nombreHeure;
            pointPrevision.setEcart(BigDecimal.valueOf(ecartMoyen));
            // MAPE


            // Observation moyenne
        });
    }
}
