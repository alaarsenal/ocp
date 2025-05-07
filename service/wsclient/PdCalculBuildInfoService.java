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
import ca.qc.hydro.epd.service.wsclient.dto.PdCalculBuildInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PdCalculBuildInfoService {

    public static final String URI = "/api/v1/build-info";

    private final WebClient pdCalculWebClient;

    public PdCalculBuildInfo getBuildInfo() throws WebClientException {
        log.info("Appel vers PdCalcul:build-info");

        try {
            PdCalculBuildInfo pdCalculBuildInfo = pdCalculWebClient.get()
                    .uri(builder -> builder.path(URI).build())
                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(PdCalculBuildInfo.class)
                    .block();

            log.info("Retour de PdCalcul:buildInfo : {}", pdCalculBuildInfo);

            if (pdCalculBuildInfo == null) {
                return null;
            }

            // extraction de la branche
            if (pdCalculBuildInfo.getBranch() == null) {
                pdCalculBuildInfo.setBranch(getBranchFromVersion(pdCalculBuildInfo));
            }

            return pdCalculBuildInfo;
        } catch (Exception e) {
            var message = new ApiMessage();
            message.setMessage(ApiMessageCode.WS_CLIENT_ERROR.getDefaultMessage() + ": " + e.getLocalizedMessage());
            message.setLevel(ApiMessageLevel.ERROR);
            message.setCode(ApiMessageCode.WS_CLIENT_ERROR.getCode());
            throw new WebClientException(Collections.singletonList(message));
        }
    }

    private String getBranchFromVersion(PdCalculBuildInfo pdCalculBuildInfo) {
        var pdCalculVersion = pdCalculBuildInfo.getVersion();
        String branch = null;
        if (pdCalculVersion != null) {
            var splitVersion = pdCalculVersion.split("-");
            if (splitVersion.length > 1) {
                branch = splitVersion[1];
                if (branch != null) {
                    branch = branch.split("\\.")[0];
                    switch (branch) {
                        case "beta" -> branch = "develop";
                        case "rc" -> branch = pdCalculBuildInfo.getLivraison();
                        case "feature", "bugfix", "hotfix" -> branch = extractBranchFromVersion(pdCalculVersion);
                        default -> branch = null;
                    }
                }
            } else {
                branch = "master";
            }
        }
        return branch;
    }

    private String extractBranchFromVersion(String pdCalculVersion) {
        String branch;
        int firstIndexOfBranch = pdCalculVersion.indexOf("-") + 1;
        int lastIndexOfbranch = pdCalculVersion.lastIndexOf(".");
        branch = pdCalculVersion.substring(firstIndexOfBranch, lastIndexOfbranch);
        branch = branch.replaceFirst("-", "/");
        return branch;
    }

}
