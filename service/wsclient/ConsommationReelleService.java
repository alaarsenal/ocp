package ca.qc.hydro.epd.service.wsclient;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.service.wsclient.dto.HistoConsRequest;
import ca.qc.hydro.epd.service.wsclient.dto.HistoConsResponse;
import ca.qc.hydro.epd.service.wsclient.dto.MessageRetour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsommationReelleService {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String MSG_AUCUNE_DONNEE_TROUVEE = "PDC8";

    private final WebClient pdCalculWebClient;
    private final MessageSource messageSource;

    public List<HistoConsResponse> getHistoCons(List<HistoConsRequest> requests) throws ValidationException {
        List<HistoConsResponse> responses =
                Flux.fromIterable(requests).parallel().runOn(Schedulers.boundedElastic()).flatMap(this::getHistoCons).sequential().collectList().block();

        for (HistoConsResponse response : responses) {
            if (response.getMessagesRetourWrapper() != null) {
                List<MessageRetour> errors = response.getMessagesRetourWrapper().getMessagesRetour().stream()
                        .filter(m -> !m.getNoMessage().equalsIgnoreCase(MSG_AUCUNE_DONNEE_TROUVEE)).toList();
                if (!CollectionUtils.isEmpty(errors)) {
                    throw new ValidationException(ApiMessageFactory.getErrors(errors, messageSource));
                }
            }
        }
        return responses;
    }

    private Mono<HistoConsResponse> getHistoCons(HistoConsRequest request) {
        log.debug("getHistoCons :: " + request.toString());
        return this.pdCalculWebClient.post().uri("/api/v1/lire-consommations")
                .bodyValue(request)
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .retrieve()
                .bodyToMono(HistoConsResponse.class);
    }

}
