package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.EtiquettePrev;
import ca.qc.hydro.epd.service.EtiquettePrevService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = EtiquettePrevController.CONTEXT_V1_ETIQUETTE_PREV)
@Tag(name = "EtiquettePrevController", description = "Opérations permettant la gestion des données des modèles relatives aux corrections/pondérations pour l'application PD Calcul.")
public class EtiquettePrevController {

    public static final String CONTEXT_V1_ETIQUETTE_PREV = APIConstant.CONTEXT_V1 + "etiquettes-prev";

    private final EtiquettePrevService etiquettePrevService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUTES les étiquettes reliées aux prévisions.")
    public ResponseEntity<ApiResponse<List<EtiquettePrev>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(etiquettePrevService.getAll()));
    }
}
