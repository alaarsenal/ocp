package ca.qc.hydro.epd.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.DonneesModele;
import ca.qc.hydro.epd.dto.CreateDonneesModeleResponseDto;
import ca.qc.hydro.epd.dto.DonneesModeleDto;
import ca.qc.hydro.epd.dto.DonneesModeleHistoryDto;
import ca.qc.hydro.epd.dto.JsonViews;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.service.DonneesModeleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = DonneesModeleController.CONTEXT_V1_DONNEES_MODELE)
@Tag(name = "DonneesModeleController", description = "Opérations permettant la gestion des données des modèles relatives aux corrections/pondérations pour l'application PD Calcul.")
public class DonneesModeleController {

    public static final String CONTEXT_V1_DONNEES_MODELE = APIConstant.CONTEXT_V1 + "donnees-modeles";

    private final DonneesModeleService donneesModeleService;

    @Operation(summary = "Enregistrement de nouvelles corrections/pondérations.")
    @ApiResponses(
            value = {

                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(
                                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"7001\",\"texteMessage\":\"La plage de dates pour les corrections est trop grande.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                                    )},
                                    mediaType = "application/json"
                            )
                    )}
    )
    @PostMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<CreateDonneesModeleResponseDto>> create(@Valid @RequestBody DonneesModeleDto dto) throws ValidationException, NotFoundException {
        CreateDonneesModeleResponseDto donModResponse = donneesModeleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, donModResponse));
    }

    @JsonView(JsonViews.DonneesModeleHistoryView.class)
    @GetMapping("/historique")
    @Operation(summary = "Visualiser l'historique des données modèles relatives aux corrections et/ou pondérations.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<DonneesModele>>> getHistory(@Valid @ParameterObject DonneesModeleHistoryDto dto) throws NotFoundException {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(donneesModeleService.getHistory(dto)));
    }
}
