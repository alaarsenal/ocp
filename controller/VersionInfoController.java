package ca.qc.hydro.epd.controller;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.VersionInfoDto;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.service.wsclient.PdCalculBuildInfoService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(VersionInfoController.CONTEXT_V1_VERSION_INFO)
@PropertySource(value = "classpath:git.version.json", ignoreResourceNotFound = true)
@Slf4j
public class VersionInfoController {

    public static final String CONTEXT_V1_VERSION_INFO = APIConstant.CONTEXT_V1 + "version-info";

    @Value("${git.branch}")
    private String branch;
    @Value("${git.commit.id.abbrev}")
    private String commitId;
    @Value("${git.commit.message.short}")
    private String commitMessage;
    @Value("${git.commit.time}")
    private String commitTime;
    @Value("${git.build.version}")
    private String buildVersion;
    @Value("${git.build.time}")
    private String buildTime;

    // nom de la branche généré par le CI (step: Version Info)
    private String gitBranch;

    @Value("${branch:#{null}}")
    public void setGitBranch(String branch) {
        this.gitBranch = branch;
    }

    private final PdCalculBuildInfoService pdCalculBuildInfoService;


    public VersionInfoController(PdCalculBuildInfoService pdCalculBuildInfoService) {
        this.pdCalculBuildInfoService = pdCalculBuildInfoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<VersionInfoDto>> getVersionInfo() {
        ApiResponse<VersionInfoDto> response = new ApiResponse<>(ApiResponse.Status.SUCCESS, null, null, Collections.emptyList());
        VersionInfoDto versionInfo = VersionInfoDto.builder()
                .branch(branch)
                .commitId(commitId)
                .commitMessage(commitMessage)
                .commitTime(commitTime)
                .buildVersion(buildVersion)
                .buildTime(buildTime)
                .build();
        // Dans le pipeline teamcity, on checkout un commit au lieu d'une branche.
        // Pour obtenir la branche, on utilise la branche de git.version.json généré par le CI
        if (gitBranch != null) {
            versionInfo.setBranch(StringUtils.strip(gitBranch, ",\""));
        }

        try {
            versionInfo.setPdCalculBuildInfo(pdCalculBuildInfoService.getBuildInfo());
        } catch (WebClientException e) {
            log.error("Erreur lors de l'appel vers PdCalcul:build-info", e);
            response.setMessages(e.getApiMessages());
        }

        response.setData(versionInfo);

        return ResponseEntity.ok(response);
    }

}
