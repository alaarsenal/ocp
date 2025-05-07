package ca.qc.hydro.epd.controller;

import java.time.ZoneOffset;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.PrevisionEtudeRp;
import ca.qc.hydro.epd.dto.JsonViews;
import ca.qc.hydro.epd.dto.PrevisionEtudeDto;
import ca.qc.hydro.epd.dto.PrevisionEtudeUpdateDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.EtagInvalidException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.service.PrevisionEtudeRpService;
import ca.qc.hydro.epd.utils.EtagUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = PrevisionEtudeRpController.CONTEXT_V1_PREVISION_ETUDE_RP)
@Tag(
        name = "PrevisionEtudeRpController",
        description = "Opérations permettant la gestion des lots de prévisions d'étude (point principal) pour l'application PD Calcul."
)
public class PrevisionEtudeRpController {

    public static final String CONTEXT_V1_PREVISION_ETUDE_RP = APIConstant.CONTEXT_V1 + "prev-etude-rp";

    private final MessageSource messageSource;

    private final PrevisionEtudeRpService previsionEtudeRpService;

    @PostMapping
    @JsonView(value = {JsonViews.PrevisionEtudeDetailsView.class})
    @Operation(summary = "Création d'un nouveau lot de prévision d'étude - point principal.")
    @ApiResponse(
            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
            content = @Content(
                    examples = {@ExampleObject(
                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"10002\",\"texteMessage\":\"Impossible de créer cette prévision d''étude puisqu'une prévision ayant le même code existe déjà.\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                    )},
                    mediaType = "application/json"
            )
    )
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<PrevisionEtudeRp>> create(@Valid @RequestBody PrevisionEtudeDto dto)
            throws ValidationException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, previsionEtudeRpService.create(dto)));
    }

    @PutMapping
    @JsonView(value = {JsonViews.PrevisionEtudeDetailsView.class})
    @Operation(summary = "Modifier les informations d'un lot de prévision d'étude existant - point principal.")
    @Parameter(name = "If-Match", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, description = "Numéro de version pour gérer la concurrence", example = "\"1569884789000\"")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<PrevisionEtudeRp>> update(@Parameter(hidden = true) @RequestHeader(value = HttpHeaders.IF_MATCH, required = true) final String ifMatch, @RequestBody PrevisionEtudeUpdateDto updatedDto)
            throws NotFoundException, ConcurrentEditionException, ValidationException, EtagInvalidException {

        ca.qc.hydro.epd.apierror.ApiResponse<Void> response = EtagUtils.verifyIfMatchIsPresent(ifMatch, messageSource);
        if (response != null) {
            return ResponseEntity.badRequest().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(response.getStatus(), response.getMessages()));
        }
        updatedDto.setVersion(EtagUtils.convertEtagToTime(ifMatch, messageSource));

        PrevisionEtudeRp updatedPrevEtudeRp = previsionEtudeRpService.update(updatedDto);
        return ResponseEntity.ok().eTag(Long.toString(updatedPrevEtudeRp.getDateMaj().toEpochSecond(ZoneOffset.UTC)))
                .body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, updatedPrevEtudeRp));
    }
}
