package ca.qc.hydro.epd.redshift;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.exception.EpdServerException;
import ca.qc.hydro.epd.exception.RedshiftQueryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.redshiftdata.model.DescribeStatementResponse;
import software.amazon.awssdk.services.redshiftdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.redshiftdata.model.Field;
import software.amazon.awssdk.services.redshiftdata.model.GetStatementResultResponse;
import software.amazon.awssdk.services.redshiftdata.model.SqlParameter;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedshiftQueryExecutor {

    private static final String MESSAGE_SUCCESS = "Succès de l'exécution de la requête Redshift {} en {} ms";
    private static final String MESSAGE_FAILURE = "Échec de l'exécution de la requête Redshift {} en {} ms: {}";

    @Value("${epd.aws.redshift.clusterId}")
    private String clusterId;

    @Value("${epd.aws.redshift.database}")
    private String database;

    @Value("${epd.aws.redshift.schema}")
    private String schema;

    @Value("${epd.aws.redshift.dbUser}")
    private String dbUser;

    private final RedshiftAsyncActionsService redshiftAsyncActionsService;
    private final MessageSource messageSource;

    public <T> List<T> queryForList(String query, List<SqlParameter> parameters, Function<List<Field>, T> mapper) throws RedshiftQueryException {
        try {
            return executeAndHandleQueryAsync(
                    query,
                    parameters,
                    getStatementResultResponse -> getResults(getStatementResultResponse, mapper)
            ).join();
        } catch (Exception e) {
            throw new RedshiftQueryException(ApiMessageFactory.getError(ApiMessageCode.REDSHIFT_QUERY_ERROR, new Object[]{}, messageSource));
        }
    }

    public <T> T queryForObject(String query, List<SqlParameter> parameters, Function<List<Field>, T> mapper) throws RedshiftQueryException {
        try {
            return executeAndHandleQueryAsync(
                    query,
                    parameters,
                    getStatementResultResponse -> getResults(getStatementResultResponse, mapper)
            ).thenApply(list -> list.isEmpty() ? null : list.getFirst()).join();
        } catch (Exception e) {
            throw new RedshiftQueryException(ApiMessageFactory.getError(ApiMessageCode.REDSHIFT_QUERY_ERROR, new Object[]{}, messageSource));
        }
    }

    public Long queryForLong(String query, List<SqlParameter> parameters) throws RedshiftQueryException {
        try {
            return executeAndHandleQueryAsync(
                    query,
                    parameters,
                    getStatementResultResponse -> getResults(getStatementResultResponse, fields -> fields.getFirst().longValue())
            ).thenApply(list -> list.isEmpty() ? null : list.getFirst()).join();
        } catch (Exception e) {
            throw new RedshiftQueryException(ApiMessageFactory.getError(ApiMessageCode.REDSHIFT_QUERY_ERROR, new Object[]{}, messageSource));
        }
    }

    public static SqlParameter param(String name, String value) {
        return SqlParameter.builder().name(name).value(value).build();
    }

    private <T> CompletableFuture<T> executeAndHandleQueryAsync(String query, List<SqlParameter> parameters, Function<GetStatementResultResponse, T> resultHandler) {
        if (schema != null) {
            query = query.replace("schema", schema);
        }
        ExecuteStatementRequest statementRequest = buildExecuteStatementRequest(query, parameters);

        AtomicReference<String> sqlId = new AtomicReference<>();
        AtomicReference<DescribeStatementResponse> describeStatementResponse = new AtomicReference<>();

        return redshiftAsyncActionsService.executeStatementAsync(statementRequest)
                .thenApply(statementId -> {
                    sqlId.set(statementId);
                    return statementId;
                })
                .thenCompose(redshiftAsyncActionsService::checkStatementAsync)
                .thenApply(describeStmtResponse -> {
                    describeStatementResponse.set(describeStmtResponse);
                    return describeStmtResponse;
                })
                .thenCompose(redshiftAsyncActionsService::getStatementResultAsync)
                .thenApply(resultHandler)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logFailure(String.valueOf(sqlId), describeStatementResponse.get());
                        Thread.currentThread().interrupt();
                        throw new EpdServerException(throwable.getMessage());
                    } else {
                        logSuccess(String.valueOf(sqlId), describeStatementResponse.get());
                    }
                });
    }

    private ExecuteStatementRequest buildExecuteStatementRequest(String query, List<SqlParameter> parameters) {
        ExecuteStatementRequest.Builder builder = ExecuteStatementRequest.builder()
                .clusterIdentifier(clusterId)
                .database(database)
                .sql(query)
                .dbUser(dbUser);
        if (parameters != null) {
            builder.parameters(parameters);
        }
        return builder.build();
    }

    private <T> List<T> getResults(GetStatementResultResponse getStatementResultResponse, Function<List<Field>, T> mapper) {
        return getStatementResultResponse.records().stream().map(mapper).toList();
    }

    private void logSuccess(String statementId, DescribeStatementResponse describeStatementResponse) {
        log.info(MESSAGE_SUCCESS, statementId, Instant.now().toEpochMilli() - describeStatementResponse.createdAt().toEpochMilli());
    }

    private void logFailure(String id, DescribeStatementResponse describeStatementResponse) {
        log.error(MESSAGE_FAILURE, id, Instant.now().toEpochMilli() - describeStatementResponse.createdAt().toEpochMilli(), describeStatementResponse.error());
    }

}
