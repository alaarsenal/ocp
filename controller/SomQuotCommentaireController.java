package ca.qc.hydro.epd.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.domain.SomQuotCommentaire;
import ca.qc.hydro.epd.dto.SomQuotCommentaireDto;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.SomQuotCommentaireMapper;
import ca.qc.hydro.epd.service.SomQuotCommentaireService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping(value = SomQuotCommentaireController.CONTEXT_V1_SOM_QUOT_COM)
@Tag(name = "SomQuotCommentaireController", description = "SomQuotCommentaireController")
public class SomQuotCommentaireController {

    public static final String CONTEXT_V1_SOM_QUOT_COM = APIConstant.CONTEXT_V1 + "sommaire-quotidien/commentaire";

    private final SomQuotCommentaireService somQuotCommentaireService;
    private final SomQuotCommentaireMapper somQuotCommentaireMapper;

    @PostMapping
    @Operation(summary = "Création d'un commentaire sommaire quotidien")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_CREATED, description = APIConstant.HTTP_CODE_CREATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<SomQuotCommentaireDto>> create(
            @Valid @RequestBody SomQuotCommentaireDto dto
    ) throws ValidationException {

        SomQuotCommentaire somQuotCommentaire = somQuotCommentaireMapper.toEntity(dto);
        return new ResponseEntity<>(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(somQuotCommentaireMapper.toDto(somQuotCommentaireService.create(somQuotCommentaire))),
                HttpStatus.CREATED
        );
    }
}
