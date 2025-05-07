package ca.qc.hydro.epd.service.wsclient;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.util.UriBuilder;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.dto.QueryParam;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.starter.openid.service.IOpenIdServicesManager;
import ca.qc.hydro.xfl.exception.OpenIdException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class HttpClientService {

	private  IOpenIdServicesManager openIdServicesManager;

    private String usedOpenIdProviderOption;

    private boolean enabledCall;

    private String baseUrl;

    private long timeoutInSeconds;

	HttpClientService(
			IOpenIdServicesManager openIdServicesManager,
			String usedOpenIdProviderOption,
			boolean enabledCall,
			String baseUrl,
			long timeoutInSeconds
			) {
		this.openIdServicesManager = openIdServicesManager;
		this.usedOpenIdProviderOption = usedOpenIdProviderOption;
		this.enabledCall = enabledCall;
		this.baseUrl = baseUrl;
		this.timeoutInSeconds= timeoutInSeconds;
	}

	public <T> CompletableFuture<T> asyncHttpCall(HttpMethod httpMethod, String api, Object payload, Map<String, String> headers, Class<T> clazzResp, QueryParam... params) {

		if (!enabledCall) {
			log.info("Call to CalculConfig WS is disabled");
			return CompletableFuture.completedFuture(null);
		}

		String token;

		try {
			token = openIdServicesManager
					.getTokenOpenId(usedOpenIdProviderOption).getAccessToken();
		} catch (OpenIdException e) {
			log.error("Error getting token from OpenId", e);
			return CompletableFuture.completedFuture(null);
		}

		RequestBodySpec reqBodySpec
			= WebClient.create(baseUrl)
	    		.method(httpMethod)
	    		.uri(uriBuilder -> buildUriWithParams(uriBuilder, api, params))
	    		.headers(httpHeaders -> headers.forEach(httpHeaders::add))
	    		.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
	    		.accept(MediaType.APPLICATION_JSON);

		RequestHeadersSpec<?>  reqHeadersSpec;

		if (payload != null) {
    		  reqHeadersSpec = reqBodySpec.bodyValue(payload);
	    } else {
    	      reqHeadersSpec = reqBodySpec;
		}

	    return reqHeadersSpec
	            .retrieve()
	            .onStatus(
					    status -> status.is4xxClientError() || status.is5xxServerError(),
					    response -> response.bodyToMono(String.class)
						    .map(mess -> {
	                            log.error("Erreur lors de l'appel du service distant : {}", mess);
	                            return new WebClientException(ApiMessageFactory.getError(ApiMessageCode.INTERNAL_SERVER_ERROR, null));
	                        }))
	            .bodyToMono(clazzResp)
	            .timeout(Duration.ofSeconds(timeoutInSeconds))
	            .subscribeOn(Schedulers.single())
	            .toFuture();
	}

	private URI buildUriWithParams(UriBuilder uriBuilder, String path, QueryParam...params) {

		uriBuilder.path(path);

		if(params == null) return uriBuilder.build();

		Arrays.stream(params)
			.forEach(p -> uriBuilder.queryParam(p.name(), p.value()));

		return uriBuilder.build();
	}

}
