package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.MessageDto;
import ca.qc.hydro.epd.enums.EDashboardSecurite;
import ca.qc.hydro.epd.service.DashboardMessagesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = DashboardMessagesController.CONTEXT_V1_DASHBOARD)
@Tag(name = "DashboardMessagesController", description = "DashboardMessagesController")
public class DashboardMessagesController {

    public static final String CONTEXT_V1_DASHBOARD = APIConstant.CONTEXT_V1 + "dashboard-messages";

    private final DashboardMessagesService dashboardMessagesService;

    @GetMapping()
    @Operation(summary = "Visualiser la liste de tous les messages du dashboard.")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getAll(@RequestParam(required = true) EDashboardSecurite dashboardSecurite) {
        return ResponseEntity.ok(new ApiResponse<List<MessageDto>>(dashboardMessagesService.obtenirMessages(dashboardSecurite)));
    }

}
