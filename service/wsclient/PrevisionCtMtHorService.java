package ca.qc.hydro.epd.service.wsclient;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.qc.hydro.epd.apierror.ApiMessage;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageLevel;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionCtMtHorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrevisionCtMtHorService {

    public static final String URI = "/api/v1/previsions-point-ct-mt-hor";

    private final WebClient pdCalculWebClient;

    public PrevisionCtMtHorResponse getPrevCtMtHor(String dateDebut, String datefin, String objectif) throws WebClientException {
        log.info("Appel vers PdCalcul:lirePrevPointCtMtHor avec les paramètres: jourPrevDeb = {}, jourPrevFin = {}, objectif = {}", dateDebut, datefin, objectif);

        try {
            PrevisionCtMtHorResponse previsionCtMtHorResponse = pdCalculWebClient.get()
                    .uri(builder -> builder.path(URI)
                            .queryParam("jourPrevDeb", dateDebut)
                            .queryParam("jourPrevFin", datefin)
                            .queryParam("objectif", objectif)
                            .build())
                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(PrevisionCtMtHorResponse.class)
                    .block();

            log.info("Retour de PdCalcul:lirePrevResCtMtHor : {}", previsionCtMtHorResponse);
            return previsionCtMtHorResponse;
        } catch (Exception e) {
            var message = new ApiMessage();
            message.setMessage(ApiMessageCode.WS_CLIENT_ERROR.getDefaultMessage() + ": " + e.getLocalizedMessage());
            message.setLevel(ApiMessageLevel.ERROR);
            message.setCode(ApiMessageCode.WS_CLIENT_ERROR.getCode());
            throw new WebClientException(Collections.singletonList(message));
        }
    }

}
