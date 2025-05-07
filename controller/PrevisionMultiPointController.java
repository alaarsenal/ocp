package ca.qc.hydro.epd.controller;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.CriterePointsPrevision;
import ca.qc.hydro.epd.dto.ResultatPointsPrevisions;
import ca.qc.hydro.epd.service.PrevisionMultiPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = PrevisionMultiPointController.PREVISION_MULTI_POINT_V1)
@Tag(
        name = "PrevisionMultiPointController",
        description = "Affiche les dernières prévisions des consommations par points"
)
public class PrevisionMultiPointController {
    public static final String PREVISION_MULTI_POINT_V1 = APIConstant.CONTEXT_V1 + "performance-previsions";
    private final PrevisionMultiPointService previsionMultiPointService;

    @PostMapping
    @Operation(summary = "Obtenir les performances des prévisions.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = APIConstant.HTTP_CODE_OK, description = APIConstant.HTTP_CODE_OK_MESSAGE)
    public ResponseEntity<ApiResponse<ResultatPointsPrevisions>> getPoints(@Valid @RequestBody
                                                                           CriterePointsPrevision criterePointsPrevision) {
        ResultatPointsPrevisions result = previsionMultiPointService.getPointPrevisions(criterePointsPrevision.getDateReference(),
                criterePointsPrevision.getNombreHeure());
        return ResponseEntity.ok(new ApiResponse<>(result));
    }
}
