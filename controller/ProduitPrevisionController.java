package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.ProduitPrevision;
import ca.qc.hydro.epd.service.ProduitPrevisionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = ProduitPrevisionController.CONTEXT_V1_PRODUIT_PREVISION)
@Tag(name = "ProduitPrevisionController", description = "Opérations permettant la gestion des produits de prévisions pour l'application PD Calcul.")
public class ProduitPrevisionController {

    public static final String CONTEXT_V1_PRODUIT_PREVISION = APIConstant.CONTEXT_V1 + "produit-prev";

    private final ProduitPrevisionService produitPrevisionService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les produits de prévisions.")
    public ResponseEntity<ApiResponse<List<ProduitPrevision>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(produitPrevisionService.getAll()));
    }
}
