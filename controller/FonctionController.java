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
import ca.qc.hydro.epd.domain.Fonction;
import ca.qc.hydro.epd.service.FonctionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = FonctionController.CONTEXT_V1_FONCTION)
@Tag(name = "FonctionController", description = "Opérations permettant la gestion des données des modèles pour l'application PD Calcul.")
public class FonctionController {

    static final String CONTEXT_V1_FONCTION = APIConstant.CONTEXT_V1 + "fonctions";

    private final FonctionService fonctionService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUTES les fonctions, pour une ou plusieurs combinaison(s) de point(s)/modèle(s) spécifique(s).")
    public ResponseEntity<ApiResponse<List<Fonction>>> findFonctionsByPointsAndModeles(
            @Parameter(name = "codesRefPoints", description = "Codes Ref des points pour lesquels on veut récupérer la liste des profils spéciaux") @RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints,
            @Parameter(name = "codesMod", description = "Codes des modèles pour lesquels on veut récupérer la liste des profils spéciaux") @RequestParam(value = "codesMod", required = false) List<String> codesModeles
    ) {
        return ResponseEntity.ok(new ApiResponse<>(fonctionService.getFonctionsByPointsAndModeles(codesRefPoints, codesModeles)));
    }

}
