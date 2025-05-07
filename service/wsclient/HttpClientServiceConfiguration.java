package ca.qc.hydro.epd.service.wsclient;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ca.qc.hydro.starter.openid.service.IOpenIdServicesManager;


@Configuration
public class HttpClientServiceConfiguration {

    @Bean("calculConfigService")
    HttpClientService calculConfigService(
            IOpenIdServicesManager openIdServicesManager,
            @Value("${ca.qc.hydro.starter.openid.usedOpenIdProviderOption}") String usedOpenIdProviderOption,
            @Value("${hq.calculconfig.ws.client.enabledCall:false}") boolean enabledCall,
            @Value("${hq.calculconfig.ws.client.base-url}") String baseUrl,
            @Value("${hq.calculconfig.ws.client.timeout:15}") long timeoutInSeconds) {
        return new HttpClientService(openIdServicesManager, usedOpenIdProviderOption, enabledCall, baseUrl, timeoutInSeconds);
    }
}
