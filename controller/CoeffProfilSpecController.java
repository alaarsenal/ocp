package ca.qc.hydro.epd.controller;

import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;

import ca.qc.hydro.epd.service.ProfilBaseService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.domain.ProfilSpec;
import ca.qc.hydro.epd.dto.CoeffProfilSpecCreateDto;
import ca.qc.hydro.epd.dto.CoeffProfilSpecDto;
import ca.qc.hydro.epd.dto.CoeffProfilSpecIdDto;
import ca.qc.hydro.epd.dto.CoeffProfilSpecUpdateDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.EtagInvalidException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.CoeffProfilSpecIdMapper;
import ca.qc.hydro.epd.mapper.CoeffProfilSpecMapper;
import ca.qc.hydro.epd.service.CoeffProfilSpecService;
import ca.qc.hydro.epd.utils.EtagUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = CoeffProfilSpecController.CONTEXT_V1_COEFF_PROFIL_SPEC)
@Tag(
        name = "CoeffProfilSpecController",
        description = "Opérations permettant la gestion des données des courbes de profils spéciaux pour l'application PD Calcul."
)
public class CoeffProfilSpecController {

    public static final String CONTEXT_V1_COEFF_PROFIL_SPEC = APIConstant.CONTEXT_V1 + "coeff-profil-spec";

    private final CoeffProfilSpecIdMapper coeffProfilSpecIdMapper;
    private final CoeffProfilSpecService coeffProfilSpecService;
    private final CoeffProfilSpecMapper coeffProfilSpecMapper;
    private final ProfilBaseService profilBaseService;

    private final MessageSource messageSource;

    @Operation(summary = "Création de nouvelles valeurs de profil.")
    @ApiResponses(
            value = {@ApiResponse(
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
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<CoeffProfilSpecDto>>> create(@Valid @RequestBody CoeffProfilSpecCreateDto dto)
            throws ValidationException, SQLException, NotFoundException {
        List<CoeffProfilSpecDto> coeffList = coeffProfilSpecMapper.toDtoList(coeffProfilSpecService.create(dto));
        profilBaseService.synchronize();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, coeffList));
    }

    @Operation(summary = "Supprimer une ou plusieurs courbe(s) de profils spéciaux.")
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                    content = @Content(
                            examples = {@ExampleObject(
                                    value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"8004\",\"texteMessage\":\"Impossible de supprimer un profil qui est déjà associé au calendrier d’exécution.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                            )},
                            mediaType = "application/json"
                    )
            )}
    )
    @PostMapping("/batch-delete")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> deleteAll(@Valid @RequestBody List<CoeffProfilSpecIdDto> idDtos)
            throws NotFoundException, ValidationException {
        coeffProfilSpecService.deleteAll(coeffProfilSpecIdMapper.toCoeffProfilSpecId(idDtos));
        profilBaseService.synchronize();
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(null));
    }

    @GetMapping
    @Operation(summary = "Visualiser les détails d'une courbe de profil.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<CoeffProfilSpecDto>> get(@Valid CoeffProfilSpecIdDto id) throws NotFoundException {
        CoeffProfilSpecDto coeffProfilSpec = coeffProfilSpecMapper.toDto(coeffProfilSpecService.get(coeffProfilSpecIdMapper.toCoeffProfilSpecId(id)));
        return ResponseEntity.ok().eTag(Long.toString(coeffProfilSpec.getDateMaj().toEpochSecond(ZoneOffset.UTC)))
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, coeffProfilSpec));
    }

    @GetMapping("/annees")
    @Operation(
            summary = "Visualiser la liste des années distinctes, pour une ou plusieurs combinaison(s) de point(s)/modèle(s)/profil(s) spécifique(s), pour lesquels il existe au moins une courbe de profils spéciaux."
    )
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Integer>>> getAnneesByPointsAndModelesAndProfils(
            @Parameter(
                    name = "codesRefPoints", required = false,
                    description = "Codes Ref des points pour lesquels on veut récupérer la liste des années"
            ) @RequestParam(
                    value = "codesRefPoints",
                    required = false
            ) List<String> codesRefPoints,
            @Parameter(
                    name = "codesMod",
                    description = "Codes des modèles pour lesquels on veut récupérer la liste des années"
            ) @RequestParam(
                    value = "codesMod",
                    required = false
            ) List<String> codesModeles,
            @Parameter(
                    name = "profils",
                    description = "Numéros des profils pour lesquels on veut récupérer la liste des années"
            ) @RequestParam(
                    value = "profils",
                    required = false
            ) List<Integer> profils
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                coeffProfilSpecService.getAnneesByPointsAndModelesAndProfils(codesRefPoints, codesModeles, profils)));
    }

    @GetMapping("/modeles")
    @Operation(
            summary = "Visualiser la liste des modèles distincts, pour une ou plusieurs combinaison(s) de point(s) spécifique(s), pour lesquels il existe au moins une courbe de profils spéciaux."
    )
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> getModelesByPoints(
            @Parameter(
                    name = "codesRefPoints",
                    description = "Codes Ref des points pour lesquels on veut récupérer la liste des modèles"
            ) @RequestParam(
                    value = "codesRefPoints",
                    required = false
            ) List<String> codesRefPoints
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(coeffProfilSpecService.getModelesByPoints(codesRefPoints)));
    }

    @GetMapping("/profil-spec")
    @Operation(
            summary = "Visualiser la liste des profils spéciaux distincts, pour une ou plusieurs combinaison(s) de point(s)/modèle(s) spécifique(s), pour lesquels il existe au moins une courbe de profils spéciaux."
    )
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<ProfilSpec>>> getProfilsByPointsAndModeles(
            @Parameter(
                    name = "codesRefPoints",
                    description = "Codes Ref des points pour lesquels on veut récupérer la liste des profils spéciaux"
            ) @RequestParam(
                    value = "codesRefPoints",
                    required = false
            ) List<String> codesRefPoints,
            @Parameter(
                    name = "codesMod",
                    description = "Codes des modèles pour lesquels on veut récupérer la liste des profils spéciaux"
            ) @RequestParam(
                    value = "codesMod",
                    required = false
            ) List<String> codesModeles
    ) {
        return ResponseEntity
                .ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(coeffProfilSpecService.getProfilsByPointsAndModeles(codesRefPoints, codesModeles)));
    }

    @PatchMapping("/coeff-profil")
    @Operation(summary = "Modifier les valeurs de profil.")
    @Parameter(
            name = "If-Match", schema = @Schema(type = "string"), in = ParameterIn.HEADER,
            description = "Numéro de version pour gérer la concurrence", example = "\"1569884789000\""
    )
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<CoeffProfilSpecDto>>> update(
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.IF_MATCH) final String ifMatch,
            @Valid @RequestBody CoeffProfilSpecUpdateDto updateDto
    )
            throws EtagInvalidException, NotFoundException, ConcurrentEditionException, SQLException {

        ca.qc.hydro.epd.apierror.ApiResponse<Void> response = EtagUtils.verifyIfMatchIsPresent(ifMatch, messageSource);
        if (response != null) {
            return ResponseEntity.badRequest().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(response.getStatus(), response.getMessages()));
        }
        updateDto.setVersion(EtagUtils.convertEtagToTime(ifMatch, messageSource));

        List<CoeffProfilSpecDto> updatedList = coeffProfilSpecMapper.toDtoList(coeffProfilSpecService.update(updateDto));
        profilBaseService.synchronize();
        return ResponseEntity.ok().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, updatedList));
    }
}
