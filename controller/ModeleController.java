package ca.qc.hydro.epd.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.dto.CodeGrpDto;
import ca.qc.hydro.epd.service.ModeleService;

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
@RequestMapping(value = ModeleController.CONTEXT_V1_MODELE)
@Tag(name = "ModeleController", description = "Opérations permettant la gestion des données des modèles pour l'application PD Calcul.")
public class ModeleController {

    static final String CONTEXT_V1_MODELE = APIConstant.CONTEXT_V1 + "modeles";

    private final ModeleService modeleService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les modèles, pour un ou plusieurs point(s).")
    public ResponseEntity<ApiResponse<List<Modele>>> findModelesByPoints(
            @Parameter(name = "codesRefPoints", description = "Codes Ref des points pour lesquels on veut récupérer la liste des modèles")
            @RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints
    ) {
        return ResponseEntity.ok(new ApiResponse<>(modeleService.getModelesByPoints(codesRefPoints)));
    }

    @PostMapping
    @Operation(summary = "Visualiser la liste de TOUS les modèles, pour un groupe point.")
    public ResponseEntity<ApiResponse<List<Modele>>> findModelesByGrp(@Valid @RequestBody() CodeGrpDto dto) {
        List<Modele> modeles = modeleService.getModelesByGroupement(dto.getCodeGrp(), null);
        return ResponseEntity.ok(new ApiResponse<>(modeles));
    }

    @GetMapping("/modeles-corr")
    @Operation(summary = "Visualiser la liste de TOUS les modèles corrigeables (indice de configuration à 'O'), pour un ou plusieurs point(s).")
    @Parameter(name = "codesRefPoints", description = "Codes des points pour lesquels on veut récupérer la liste des modèles")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> findModelesCorrByPoints(
            @RequestParam(value = "codesRefPoints") String... codesRefPoints
    ) {
        return ResponseEntity.ok(new ApiResponse<>(modeleService.getModelesCorrigeablesByPoints(List.of(codesRefPoints))));
    }

    @PostMapping("/modeles-corr")
    @Operation(summary = "Visualiser la liste de TOUS les modèles corrigeables (indice de configuration à 'O'), pour un groupe de points.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> findModelesCorrByGrp(
            @Valid @RequestBody() CodeGrpDto dto
    ) {
        List<Modele> modeles = modeleService.getModelesByGroupement(dto.getCodeGrp(), true);
        return ResponseEntity.ok(new ApiResponse<>(modeles));
    }

    @GetMapping("/modeles-pond")
    @Operation(summary = "Visualiser la liste de TOUS les modèles pondérables (indice de configuration à 'O'), pour un ou plusieurs point(s).")
    @Parameter(name = "codesRefPoints", description = "Codes des points pour lesquels on veut récupérer la liste des modèles")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> findModelesPondByPoints(
            @RequestParam(value = "codesRefPoints") String... codesRefPoints
    ) {
        return ResponseEntity.ok(new ApiResponse<>(modeleService.getModelesCorrigeablesByPoints(List.of(codesRefPoints))));
    }

    @PostMapping("/modeles-pond")
    @Operation(summary = "Visualiser la liste de TOUS les modèles pondérables (indice de configuration à 'O'), pour un groupe de points.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> findModelesPondByGrp(
            @Valid @RequestBody() CodeGrpDto dto
    ) {
        List<Modele> modeles = modeleService.getModelesPondByPoints(dto.getCodeGrp());
        return ResponseEntity.ok(new ApiResponse<>(modeles));
    }

}
