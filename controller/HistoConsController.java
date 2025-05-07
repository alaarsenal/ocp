package ca.qc.hydro.epd.controller;

import java.sql.SQLException;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.HistoCons;
import ca.qc.hydro.epd.dto.HistoConsDto;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.service.HistoConsService;

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
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping(value = HistoConsController.CONTEXT_V1_HISTO_CONS)
@Tag(name = "HistoConsController", description = "Opérations permettant la gestion des données de l'historique des consommations pour l'application PD Calcul.")
public class HistoConsController {

    public static final String CONTEXT_V1_HISTO_CONS = APIConstant.CONTEXT_V1 + "histo-cons";

    private final HistoConsService histoConsService;

    @Operation(summary = "Création de nouvelles courbes de correction/panne/appel au public.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(
                                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"8005\",\"texteMessage\":\"Impossible de créer ce profil puisqu''un profil spécial existe déjà pour cette combinaison de point, modèle, numéro et année.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                                    )},
                                    mediaType = "application/json"
                            )
                    )}
    )
    @PostMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<HistoCons>> create(@Valid @RequestBody HistoConsDto dto) throws ValidationException, SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, histoConsService.create(dto)));
    }

}
