package ca.qc.hydro.epd.service.wsclient;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.qc.hydro.epd.apierror.ApiMessage;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageLevel;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.service.wsclient.dto.JolokiaRequest;
import ca.qc.hydro.epd.utils.Constantes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PdCalculJolokiaService {

    private final WebClient pdCalculWebClient;

    public void rafraichirCache(String operation) throws WebClientException {
        log.info("Appel vers PdCalcul pour exécuter l'opération {}/{}", Constantes.PD_CALCUL_CACHE_SERVICE, operation);
        var request = new JolokiaRequest("exec", Constantes.PD_CALCUL_CACHE_SERVICE, operation);

        String response;
        try {
            response = this.pdCalculWebClient.post().uri("/jolokia/")
                    .bodyValue(request)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Succès de l'appel à PdCalcul pour exécuter l'opération {}/{} : {}", Constantes.PD_CALCUL_CACHE_SERVICE, operation, response);
        } catch (Exception e) {
            var message = new ApiMessage();
            message.setMessage(ApiMessageCode.WS_CLIENT_ERROR.getDefaultMessage() + ": " + e.getLocalizedMessage());
            message.setLevel(ApiMessageLevel.ERROR);
            message.setCode(ApiMessageCode.WS_CLIENT_ERROR.getCode());
            log.error("Erreur lors de l'appel à PdCalcul pour exécuter l'opération {}/{} : {}", Constantes.PD_CALCUL_CACHE_SERVICE, operation, e.getLocalizedMessage());
            throw new WebClientException(Collections.singletonList(message));
        }
    }

}
