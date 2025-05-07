package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.Borne;
import ca.qc.hydro.epd.service.BorneService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = BorneController.CONTEXT_V1_BORNE)
@Tag(name = "BorneController", description = "Opérations permettant la gestion des bornes pour l'application PD Calcul.")
public class BorneController {

    public static final String CONTEXT_V1_BORNE = APIConstant.CONTEXT_V1 + "borne";

    private final BorneService borneService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les codes de bornes.")
    public ResponseEntity<ApiResponse<List<Borne>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(borneService.getAll()));
    }
}
