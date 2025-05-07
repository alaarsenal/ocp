package ca.qc.hydro.epd.redshift;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.exception.EpdServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataAsyncClient;
import software.amazon.awssdk.services.redshiftdata.model.DescribeStatementRequest;
import software.amazon.awssdk.services.redshiftdata.model.DescribeStatementResponse;
import software.amazon.awssdk.services.redshiftdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.redshiftdata.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.redshiftdata.model.GetStatementResultRequest;
import software.amazon.awssdk.services.redshiftdata.model.GetStatementResultResponse;
import software.amazon.awssdk.services.redshiftdata.model.StatusString;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedshiftAsyncActionsService {

    private final RedshiftDataAsyncClient redshiftDataAsyncClient;

    public CompletableFuture<String> executeStatementAsync(ExecuteStatementRequest statementRequest) {
        return redshiftDataAsyncClient.executeStatement(statementRequest)
                .thenApply(ExecuteStatementResponse::id)
                .exceptionally(exception -> {
                    log.error("Error executing statement: {}", exception.getMessage());
                    throw new EpdServerException("Error executing statement", exception);
                });
    }

    public CompletableFuture<DescribeStatementResponse> checkStatementAsync(String sqlId) {
        DescribeStatementRequest statementRequest = DescribeStatementRequest.builder()
                .id(sqlId)
                .build();

        return redshiftDataAsyncClient.describeStatement(statementRequest)
                .thenCompose(response -> {
                    StatusString status = response.status();
                    log.debug("Status of statement {} : {} ", sqlId, status);

                    if (StatusString.FAILED.equals(status) || StatusString.ABORTED.equals(status)) {
                        throw new EpdServerException("Statement failed");
                    } else if (StatusString.FINISHED.equals(status)) {
                        return CompletableFuture.completedFuture(response);
                    } else {
                        // Sleep for 500 ms and recheck status
                        return CompletableFuture.runAsync(() -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new EpdServerException("Error during sleep: " + e.getMessage(), e);
                            }
                        }).thenCompose(ignore -> checkStatementAsync(sqlId)); // Recursively call until status is FINISHED or FAILED
                    }
                }).handle((result, exception) -> {
                    if (exception != null) {
                        throw new EpdServerException("Error checking statement", exception);
                    } else {
                        log.debug("The statement {} is finished", sqlId);
                        return result;
                    }
                });
    }

    public CompletableFuture<GetStatementResultResponse> getStatementResultAsync(DescribeStatementResponse describeStatementResponse) {
        if (StatusString.FINISHED.equals(describeStatementResponse.status())) {
            GetStatementResultRequest getStatementResultRequest = GetStatementResultRequest.builder()
                    .id(describeStatementResponse.id())
                    .build();
            return redshiftDataAsyncClient.getStatementResult(getStatementResultRequest);
        } else {
            throw new EpdServerException("Statement failed");
        }
    }

}
