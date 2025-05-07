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
import ca.qc.hydro.epd.service.wsclient.dto.MessageRetour;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionNMomentsRequest;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionNMomentsResponse;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionPlusRecentCalcRequest;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionPlusRecentCalcResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrevDemandePointService {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String MSG_AUCUNE_DONNEE_TROUVEE = "PDC8";

    private final WebClient pdCalculWebClient;
    private final MessageSource messageSource;

    public List<PrevisionNMomentsResponse> getPrevisionsNMoments(List<PrevisionNMomentsRequest> requests) throws ValidationException {
        List<PrevisionNMomentsResponse> responses =
                Flux.fromIterable(requests).parallel().runOn(Schedulers.boundedElastic()).flatMap(this::getPrevisionNMoments).sequential().collectList().block();

        for (PrevisionNMomentsResponse response : responses) {
            if (response.getMessagesRetourWrapper() != null) {
                List<MessageRetour> errors = response.getMessagesRetourWrapper().getMessagesRetour().stream()
                        .filter(m -> !m.getNoMessage().equalsIgnoreCase(MSG_AUCUNE_DONNEE_TROUVEE)).toList();
                if (!CollectionUtils.isEmpty(errors))
                    throw new ValidationException(ApiMessageFactory.getErrors(errors, messageSource));
            }
        }
        return responses;
    }

    private Mono<PrevisionNMomentsResponse> getPrevisionNMoments(PrevisionNMomentsRequest request) {
        return this.pdCalculWebClient.post().uri("/api/v1/lire-prevision/lire-prevision-n-moments-d-avance")
                .bodyValue(request)
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PrevisionNMomentsResponse.class);
    }

    public List<PrevisionPlusRecentCalcResponse> getPrevisionsPlusRecentCalc(List<PrevisionPlusRecentCalcRequest> requests) throws ValidationException {
        List<PrevisionPlusRecentCalcResponse> responses =
                Flux.fromIterable(requests).parallel().runOn(Schedulers.boundedElastic()).flatMap(this::getPrevisionPlusRecentCalc).sequential().collectList().block();

        for (PrevisionPlusRecentCalcResponse response : responses) {
            if (response.getMessagesRetourWrapper() != null) {
                List<MessageRetour> errors = response.getMessagesRetourWrapper().getMessagesRetour().stream()
                        .filter(m -> !m.getNoMessage().equalsIgnoreCase(MSG_AUCUNE_DONNEE_TROUVEE)).toList();
                if (!CollectionUtils.isEmpty(errors))
                    throw new ValidationException(ApiMessageFactory.getErrors(errors, messageSource));
            }
        }
        return responses;
    }

    private Mono<PrevisionPlusRecentCalcResponse> getPrevisionPlusRecentCalc(PrevisionPlusRecentCalcRequest request) {
        log.debug("getPrevisionPlusRecentCalc :: " + request.toString());
        return this.pdCalculWebClient.post().uri("/api/v1/lire-prevision/lire-prevision-plus-recent-calcul")
                .bodyValue(request)
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PrevisionPlusRecentCalcResponse.class);
    }

}
