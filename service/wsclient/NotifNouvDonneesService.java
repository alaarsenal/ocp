package ca.qc.hydro.epd.service.wsclient;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.qc.hydro.epd.service.wsclient.dto.NotifNouvDonneesRequest;
import ca.qc.hydro.epd.service.wsclient.dto.NotifNouvDonneesResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifNouvDonneesService {

    public static final String URI = "/api/v1/controleur/notif-nouv-donnees/lancer";

    private final WebClient pdCalculWebClient;

    public Mono<NotifNouvDonneesResponse> lancerNotifNouvDonnees(String codeProduit, String codeUtEmettrice) {
        return pdCalculWebClient.post().uri(URI)
                .bodyValue(new NotifNouvDonneesRequest(codeProduit, codeUtEmettrice))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(NotifNouvDonneesResponse.class);
    }

}
