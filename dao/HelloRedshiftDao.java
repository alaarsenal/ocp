package ca.qc.hydro.epd.dao;

import static ca.qc.hydro.epd.redshift.RedshiftQueryExecutor.param;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.dto.ConsbrutDto;
import ca.qc.hydro.epd.dto.Order;
import ca.qc.hydro.epd.dto.PageDto;
import ca.qc.hydro.epd.exception.RedshiftQueryException;
import ca.qc.hydro.epd.redshift.RedshiftQueryExecutor;
import ca.qc.hydro.epd.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.redshiftdata.model.Field;
import software.amazon.awssdk.services.redshiftdata.model.SqlParameter;

@Component
@RequiredArgsConstructor
public class HelloRedshiftDao {

    private static final String DATE_DEBUT = "date_debut";
    private static final String DATE_FIN = "date_fin";
    private static final String CODE_PT = "code_pt";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RedshiftQueryExecutor redshiftQueryExecutor;

    public List<ConsbrutDto> findConsbruts(LocalDateTime dateDebut, LocalDateTime dateFin, String codePt) throws RedshiftQueryException {
        return redshiftQueryExecutor.queryForList(
                """
                         SELECT dateutc,
                                minute,
                                codept,
                                cons,
                                ishae
                         FROM schema.consbrut
                        WHERE dateutc >= TO_TIMESTAMP(:date_debut, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                              AND dateutc <= TO_TIMESTAMP(:date_fin, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                              AND codept = :code_pt;
                        """,
                List.of(
                        param(DATE_DEBUT, dateDebut.format(DateTimeFormatter.ISO_DATE_TIME)),
                        param(DATE_FIN, dateFin.format(DateTimeFormatter.ISO_DATE_TIME)),
                        param(CODE_PT, codePt)
                ),
                mapper()
        );
    }

    public Page<ConsbrutDto> findConsbruts(LocalDateTime dateDebut, LocalDateTime dateFin, String codePt, PageDto pageDto) throws RedshiftQueryException {
        String dataQuery = """
                SELECT dateutc,
                       minute,
                       codept,
                       cons,
                       ishae
                FROM schema.consbrut
                WHERE dateutc >= TO_TIMESTAMP(:date_debut, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                      AND dateutc <= TO_TIMESTAMP(:date_fin, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                      AND codept = :code_pt
                LIMIT :limit OFFSET :offset
                """;

        String countQuery = """
                SELECT COUNT(*)
                FROM schema.consbrut
                WHERE dateutc >= TO_TIMESTAMP(:date_debut, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                      AND dateutc <= TO_TIMESTAMP(:date_fin, 'YYYY-MM-DD\\"T\\"HH24:MI:SS')
                      AND codept = :code_pt
                """;

        List<SqlParameter> parameters = List.of(
                param(DATE_DEBUT, dateDebut.format(DateTimeFormatter.ISO_DATE_TIME)),
                param(DATE_FIN, dateFin.format(DateTimeFormatter.ISO_DATE_TIME)),
                param(CODE_PT, codePt),
                param("limit", String.valueOf(pageDto.getSize())),
                param("offset", String.valueOf(pageDto.getOffset()))
        );

        List<SqlParameter> countParameters = List.of(
                param(DATE_DEBUT, dateDebut.format(DateTimeFormatter.ISO_DATE_TIME)),
                param(DATE_FIN, dateFin.format(DateTimeFormatter.ISO_DATE_TIME)),
                param(CODE_PT, codePt)
        );

        Long count = redshiftQueryExecutor.queryForLong(countQuery, countParameters);
        Pageable pageable = PaginationUtils.getPageable(pageDto);

        if (count == 0) {
            return PageableExecutionUtils.getPage(Collections.emptyList(), pageable, () -> count);
        }

        List<ConsbrutDto> data = redshiftQueryExecutor.queryForList(dataQuery, parameters, mapper());

        if (Objects.nonNull(pageDto.getOrders()) && !pageDto.getOrders().isEmpty()) {
            data.sort(Order.getComparator(pageDto.getOrders()));
        }

        return PageableExecutionUtils.getPage(data, pageable, () -> count);
    }

    public static Function<List<Field>, ConsbrutDto> mapper() {
        return fields -> ConsbrutDto.builder()
                .dateUtc(LocalDateTime.parse(fields.getFirst().stringValue(), DATETIME_FORMATTER))
                .minute(Math.toIntExact(fields.get(1).longValue()))
                .codePt(fields.get(2).stringValue())
                .cons(fields.get(3).doubleValue())
                .isHae(fields.get(4).booleanValue())
                .build();
    }

}
