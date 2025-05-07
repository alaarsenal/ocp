package ca.qc.hydro.epd.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.NMomentAvanceConsommationsRequestDto;
import ca.qc.hydro.epd.dto.NMomentAvanceDto;
import ca.qc.hydro.epd.dto.NMomentAvancePrevisionsRequestDto;
import ca.qc.hydro.epd.dto.PrevisionDto;
import ca.qc.hydro.epd.service.NMomentsAvanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-05-19
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = NMomentsAvanceController.CONTEXT_V1_N_MOMENTS_AVANCE)
@Tag(name = "NMomentsAvanceController", description = "NMomentsAvanceController")
public class NMomentsAvanceController {

    public static final String CONTEXT_V1_N_MOMENTS_AVANCE = APIConstant.CONTEXT_V1 + "n-moments-avance";

    private final NMomentsAvanceService nMomentsAvanceService;

    @PostMapping("/previsions")
    @Operation(summary = "Récupérer les prévisions n moments d'avance")
    public ResponseEntity<ApiResponse<List<NMomentAvanceDto>>> getPrevisionsNMomentsAvance(@Valid @RequestBody NMomentAvancePrevisionsRequestDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(nMomentsAvanceService.getPrevisionsNMomentsAvance(
                dto.getDateDebut().toLocalDateTime(), dto.getDateFin().toLocalDateTime(),
                dto.getCodeProduitPrev(),
                dto.getProjections(),
                dto.getMinutesPrevision(),
                dto.getCodesRefPoints(),
                dto.getCodeFonction(),
                dto.getCodeModele(),
                dto.getCodesTypePrevision()
        )));
    }

    @PostMapping("/consommations")
    @Operation(summary = "Récupérer les consommations n moments d'avance")
    public ResponseEntity<ApiResponse<List<PrevisionDto>>> getConsommations(@Valid @RequestBody NMomentAvanceConsommationsRequestDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(nMomentsAvanceService.getConsommationsNMomentsAvance(
                dto.getDateDebut().toLocalDateTime(), dto.getDateFin().toLocalDateTime(),
                dto.getMinutesPrevision(),
                dto.getCodesRefPoints()
        )));
    }

}
