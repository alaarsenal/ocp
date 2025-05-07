package ca.qc.hydro.epd.controller;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.PrevisionEtude;
import ca.qc.hydro.epd.dto.JsonViews;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.service.PrevisionEtudeService;

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
@RequestMapping(value = PrevisionEtudeController.CONTEXT_V1_PREVISION_ETUDE)
@Tag(name = "PrevisionEtudeController", description = "Opérations permettant la gestion des lots de prévisions d'étude pour l'application PD Calcul.")
public class PrevisionEtudeController {

    public static final String CONTEXT_V1_PREVISION_ETUDE = APIConstant.CONTEXT_V1 + "prev-etude";

    private final PrevisionEtudeService previsionEtudeService;

    @GetMapping("/{codePrevEtud}")
    @JsonView(value = {JsonViews.PrevisionEtudeDetailsView.class})
    @Operation(summary = "Visualiser les détails d'un lot de prévision d'étude.")
    public ResponseEntity<ApiResponse<PrevisionEtude>> get(@PathVariable("codePrevEtud") String codePrevEtud) throws NotFoundException {
        PrevisionEtude prevEtude = previsionEtudeService.getByCode(codePrevEtud);
        return ResponseEntity.ok().eTag(Long.toString(prevEtude.getDateMaj().toEpochSecond(ZoneOffset.UTC)))
                .body(new ApiResponse<>(ApiResponse.Status.SUCCESS, prevEtude));
    }

    @GetMapping("/proprietaires")
    @Operation(summary = "Visualiser la liste de TOUS les propriétaires des lots de prévisions d'étude.")
    public ResponseEntity<ApiResponse<List<String>>> getAllProprietaires() {
        return ResponseEntity.ok(new ApiResponse<>(previsionEtudeService.getAllProprietaires()));
    }

    @GetMapping
    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Operation(summary = "Visualiser la liste des lots de prévisions d'étude.")
    @Parameter(name = "proprietaires", required = false, description = "Noms d'usager des propriétaires pour lesquels on veut récupérer la liste des lots de prévision d'étude")
    public ResponseEntity<ApiResponse<List<PrevisionEtude>>> search(@RequestParam(value = "proprietaires", required = false) List<String> proprietaires) {
        return ResponseEntity.ok(new ApiResponse<>(previsionEtudeService.searchByProprietaires(proprietaires)));
    }

}
