package ca.qc.hydro.epd.controller;

import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.GroupePoint;
import ca.qc.hydro.epd.dto.GroupePointDto;
import ca.qc.hydro.epd.dto.GroupePointSearchCriteriaDto;
import ca.qc.hydro.epd.dto.GroupePointSearchDetailsResultDto;
import ca.qc.hydro.epd.dto.GroupePointSearchResultDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.EtagInvalidException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.GroupePointMapper;
import ca.qc.hydro.epd.mapper.GroupePointSearchResultDtoMapper;
import ca.qc.hydro.epd.service.GroupePointService;
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
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = GroupePointController.CONTEXT_V1_GROUPE_POINT)
@Tag(
        name = "GroupePointController",
        description = "Opérations permettant la gestion des données des groupements de points pour l'application PD Calcul."
)
public class GroupePointController {

    public static final String CONTEXT_V1_GROUPE_POINT = APIConstant.CONTEXT_V1 + "grp-point";

    private final MessageSource messageSource;
    private final GroupePointService groupePointService;
    private final GroupePointSearchResultDtoMapper groupePointSearchResultDtoMapper;
    private final GroupePointMapper groupePointMapper;

    @Operation(summary = "Création d'un nouveau groupement de point(s).")
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                    content = @Content(
                            examples = {@ExampleObject(
                                    value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"7001\",\"texteMessage\":\"Impossible de créer ce groupement puisqu'un groupement ayant le même code existe déjà.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                            )},
                            mediaType = "application/json"
                    )
            )}
    )
    @PostMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<GroupePoint>> create(@Valid @RequestBody GroupePointDto dto)
            throws ValidationException, ExecutionException, InterruptedException {
        GroupePoint createdGroupement = groupePointService.create(dto);
        groupePointService.synchronize();
        groupePointService.clearCacheAndSendNotification();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, createdGroupement));
    }

    @Operation(summary = "Supprimer un groupement de point(s).")
    @DeleteMapping("/{codeGrp}")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> delete(@PathVariable("codeGrp") String codeGrp)
            throws NotFoundException, ValidationException, ExecutionException, InterruptedException {
        groupePointService.delete(codeGrp);
        groupePointService.synchronize();
        groupePointService.clearCacheAndSendNotification();
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(null));
    }

    @Operation(summary = "Supprimer plusieurs groupements de point(s).")
    @DeleteMapping
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> deleteMany(@RequestParam("codesGrp") List<String> codesGrp)
            throws NotFoundException, ValidationException, ExecutionException, InterruptedException {
        groupePointService.deleteMany(codesGrp);
        groupePointService.synchronize();
        groupePointService.clearCacheAndSendNotification();
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(null));
    }

    @GetMapping("/{codeGrp}")
    @Operation(summary = "Visualiser les détails d'un groupement de point(s).")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<GroupePointDto>> get(@PathVariable("codeGrp") String codeGrp)
            throws NotFoundException {
        GroupePoint grp = groupePointService.get(codeGrp);
        return ResponseEntity.ok().eTag(Long.toString(grp.getDateMaj().toEpochSecond(ZoneOffset.UTC)))
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                        ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS,
                        groupePointMapper.toDto(grp)
                ));
    }

    @GetMapping
    @Operation(summary = "Visualiser la liste des groupements points.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<GroupePointSearchResultDto>>> search(
            GroupePointSearchCriteriaDto searchCriteria
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                groupePointSearchResultDtoMapper.toGroupePointSearchResultDto(groupePointService.search(searchCriteria))));
    }

    @GetMapping("/details")
    @Operation(summary = "Visualiser la liste des groupements points avec tous les points, les modèles et les fonctions associés.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<GroupePointSearchDetailsResultDto>>> getAllWithDetails()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(groupePointService.getWithDetails()));
    }

    @PutMapping
    @Operation(summary = "Modifier les informations d'un groupement de point(s) existant.")
    @Parameter(name = "If-Match", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, description = "Numéro de version pour gérer la concurrence", example = "\"1569884789000\"")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<GroupePoint>> update(
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.IF_MATCH) final String ifMatch,
            @RequestBody GroupePointDto updatedDto
    ) throws NotFoundException, ConcurrentEditionException, ValidationException, EtagInvalidException, ExecutionException, InterruptedException {
        ca.qc.hydro.epd.apierror.ApiResponse<Void> response = EtagUtils.verifyIfMatchIsPresent(ifMatch, messageSource);
        if (response != null) {
            return ResponseEntity.badRequest().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(response.getStatus(), response.getMessages()));
        }
        updatedDto.setVersion(EtagUtils.convertEtagToTime(ifMatch, messageSource));
        GroupePoint updatedGrpRes = groupePointService.update(updatedDto);
        groupePointService.synchronize();
        groupePointService.clearCacheAndSendNotification();
        return ResponseEntity.ok().eTag(Long.toString(updatedGrpRes.getDateMaj().toEpochSecond(ZoneOffset.UTC)))
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, updatedGrpRes));
    }
}
