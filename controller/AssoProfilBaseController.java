package ca.qc.hydro.epd.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.dto.AssoProfilBaseDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.AssoProfilBaseMapper;
import ca.qc.hydro.epd.service.ProfilBaseService;

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
@RequestMapping(value = AssoProfilBaseController.CONTEXT_V1_ASSO_PROFILS_BASE)
@Tag(name = "AssoProfilBaseController", description = "Opérations permettant la gestion d'associations de profils de base aux différents modèles de l'application PD Calcul.")
public class AssoProfilBaseController {

    public static final String CONTEXT_V1_ASSO_PROFILS_BASE = APIConstant.CONTEXT_V1 + "asso-prof-base";

    private final ProfilBaseService profilBaseService;
    private final AssoProfilBaseMapper assoProfilBaseMapper;

    @Operation(summary = "Association d'un profil de base à un modèle.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(
                                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"8001\",\"texteMessage\":\"La plage de dates pour les associations est trop grande.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                                    )},
                                    mediaType = "application/json"
                            )
                    )}
    )

    @PostMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<AssoProfilBaseDto>>> create(@Valid @RequestBody AssoProfilBaseDto dto) throws NotFoundException, ValidationException {
        List<AssoProfilBaseDto> associations = assoProfilBaseMapper.toDtoList(profilBaseService.addAsso(dto));
        profilBaseService.synchronize();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, associations));
    }
}
