package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.ProfilSpec;
import ca.qc.hydro.epd.service.ProfilSpecService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = ProfilSpecController.CONTEXT_V1_PROFIL_SPEC)
@Tag(name = "ProfilSpecController", description = "Opérations permettant la gestion des profils spéciaux pour l'application PD Calcul.")
public class ProfilSpecController {

    public static final String CONTEXT_V1_PROFIL_SPEC = APIConstant.CONTEXT_V1 + "profil-spec";

    private final ProfilSpecService profilSpecService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les profils spéciaux.")
    public ResponseEntity<ApiResponse<List<ProfilSpec>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(profilSpecService.getAll()));
    }
}
