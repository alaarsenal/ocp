package ca.qc.hydro.epd.controller;

import java.util.List;

import ca.qc.hydro.epd.service.ProfilBaseService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.dto.AssoProfilSpecDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.AssoProfilSpecMapper;
import ca.qc.hydro.epd.service.CoeffProfilSpecService;

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
@RequestMapping(value = AssoProfilSpecController.CONTEXT_V1_ASSO_PROFILS_SPEC)
@Tag(name = "AssoProfilSpecController", description = "Opérations permettant la gestion d'associations de profils spéciaux aux différents modèles de l'application PD Calcul.")
public class AssoProfilSpecController {

    public static final String CONTEXT_V1_ASSO_PROFILS_SPEC = APIConstant.CONTEXT_V1 + "asso-prof-spec";

    private final CoeffProfilSpecService coeffProfilSpecService;
    private final AssoProfilSpecMapper assoProfilSpecMapper;
    private final ProfilBaseService profilBaseService;

    @Operation(summary = "Association d'un profil spécial à un modèle.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"8001\",\"texteMessage\":\"La plage de dates pour les associations est trop grande.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}")},
                                    mediaType = "application/json"
                            )
                    )}
    )
    @PostMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<AssoProfilSpecDto>>> create(@Valid @RequestBody AssoProfilSpecDto dto) throws NotFoundException, ValidationException {
        List<AssoProfilSpecDto> associations = assoProfilSpecMapper.toDtoList(coeffProfilSpecService.addAsso(dto));
        profilBaseService.synchronize();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, associations));
    }
}
