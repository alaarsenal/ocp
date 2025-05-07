package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.SourceDonnee;
import ca.qc.hydro.epd.service.SourceDonneeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = SourceDonneeController.CONTEXT_V1_SOURCE_DONNEE)
@Tag(name = "SourceDonneeController", description = "Opérations permettant la gestion des sources de données pour l'application PD Calcul.")
public class SourceDonneeController {

    public static final String CONTEXT_V1_SOURCE_DONNEE = APIConstant.CONTEXT_V1 + "src-donnee";

    private final SourceDonneeService sourceDonneeService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUTES les sources de données.")
    public ResponseEntity<ApiResponse<List<SourceDonnee>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(sourceDonneeService.getAll()));
    }
}
