package ca.qc.hydro.epd.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.SwaggerConstant;
import ca.qc.hydro.epd.domain.CleProduitCons;
import ca.qc.hydro.epd.dto.CleProduitConsCreateDto;
import ca.qc.hydro.epd.dto.CleProduitConsDto;
import ca.qc.hydro.epd.dto.CleProduitConsSearchCriteriaDto;
import ca.qc.hydro.epd.dto.CleProduitConsUpdateDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.mapper.CleProduitConsMapper;
import ca.qc.hydro.epd.service.CleProduitConsService;
import ca.qc.hydro.epd.service.wsclient.PdCalculJolokiaService;
import ca.qc.hydro.epd.utils.Constantes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-04
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = CleProduitConsController.CONTEXT_V1_CLE_PRODUIT_CONS)
@Tag(name = "CleProduitConsController", description = "Produits d'aquisition")
public class CleProduitConsController {

    public static final String CONTEXT_V1_CLE_PRODUIT_CONS = APIConstant.CONTEXT_V1 + "produits-cons";

    private final CleProduitConsService cleProduitConsService;
    private final CleProduitConsMapper cleProduitConsMapper;
    private final PdCalculJolokiaService pdCalculJolokiaService;

    @GetMapping
    @Operation(summary = "Visualiser une page de produits d'acquisition")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Page<CleProduitConsDto>>> getPage(
            @RequestParam int page, @RequestParam int size,
            @RequestParam(required = false) String sortField, @RequestParam(required = false) Sort.Direction sortOrder,
            @RequestParam(required = false) List<String> codesGroupements,
            @RequestParam(required = false) List<String> codesRefPoints,
            @Parameter(
                    name = "dateReference", description = "Date de référence format: yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX",
                    example = "2022-03-01T13:00:00.000Z"
            ) @RequestParam(required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) OffsetDateTime dateReference
    ) {
        CleProduitConsSearchCriteriaDto searchCriteriaDto = CleProduitConsSearchCriteriaDto.builder().codesGroupements(codesGroupements)
                .codesRefPoints(codesRefPoints).dateReference(dateReference).build();
        Page<CleProduitCons> resultat = cleProduitConsService.getPage(page, size, sortField, sortOrder, searchCriteriaDto);
        if (Objects.nonNull(resultat)) {
            return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(resultat.map(cleProduitConsMapper::toDto)));
        }
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>());
    }

    @GetMapping("/{codeRefPoint}/{codeSourceDonnee}/{codeProduit}/{noCle}/{dateEnr}")
    @Operation(summary = "Visualiser une association point produit")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = SwaggerConstant.HTTP_CODE_OK, description = SwaggerConstant.HTTP_CODE_OK_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_NOT_FOUND, description = APIConstant.HTTP_CODE_NOT_FOUND_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<CleProduitConsDto>> getProduitCons(
            @PathVariable("codeRefPoint") String codeRefPoint,
            @PathVariable("codeSourceDonnee") String codeSourceDonnee,
            @PathVariable("codeProduit") String codeProduit,
            @PathVariable("noCle") String noCle,
            @Parameter(
                    required = true, schema = @Schema(format = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"),
                    example = "2019-03-27T16:35:40.000Z"
            ) @PathVariable("dateEnr") @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) OffsetDateTime dateEnr
    ) throws NotFoundException {
        CleProduitCons cleProduitCons = cleProduitConsService.getOne(codeRefPoint, codeSourceDonnee, codeProduit, noCle, dateEnr);
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(cleProduitConsMapper.toDto(cleProduitCons)));
    }

    @PostMapping
    @Operation(summary = "Créer une association point produit")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_CREATED, description = APIConstant.HTTP_CODE_CREATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<CleProduitConsDto>> create(
            @Parameter(
                    name = "codesRefPoints", required = true, description = "Codes points pour lesquels on veut créer le produit",
                    example = "BQ"
            ) @RequestParam(name = "codesRefPoints") List<String> codesRefPoints,
            @Valid @RequestBody CleProduitConsCreateDto dto
    ) throws ValidationException, NotFoundException, WebClientException {
        CleProduitCons cleProduitCons = cleProduitConsMapper.createDtoToEntity(dto);
        CleProduitCons createdCleProduitCons = cleProduitConsService.create(cleProduitCons, codesRefPoints);
        final var response = cleProduitConsMapper.toDto(createdCleProduitCons);
        cleProduitConsService.synchronize();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_CLE_PRODUIT_CONS_OPERATION);
        return new ResponseEntity<>(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{codeRefPoint}/{codeSourceDonnee}/{codeProduit}/{noCle}/{dateEnr}")
    @Operation(summary = "Modifier une association point produit")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UPDATED, description = APIConstant.HTTP_CODE_UPDATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_NOT_FOUND, description = APIConstant.HTTP_CODE_NOT_FOUND_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<CleProduitConsDto>> update(
            @PathVariable("codeRefPoint") String codeRefPoint,
            @PathVariable("codeSourceDonnee") String codeSourceDonnee,
            @PathVariable("codeProduit") String codeProduit,
            @PathVariable("noCle") String noCle,
            @Parameter(
                    required = true, schema = @Schema(format = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"),
                    example = "2019-03-27T16:35:40.000Z"
            ) @PathVariable("dateEnr") @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) OffsetDateTime dateEnr,
            @Valid @RequestBody CleProduitConsUpdateDto dto
    ) throws NotFoundException, ValidationException, WebClientException {
        CleProduitCons cleProduitCons = cleProduitConsMapper.updateDtoToEntity(dto);
        CleProduitCons updatedCleProduitCons = cleProduitConsService.update(codeRefPoint, codeSourceDonnee, codeProduit, noCle, dateEnr, cleProduitCons);
        final var response = cleProduitConsMapper.toDto(updatedCleProduitCons);
        cleProduitConsService.synchronize();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_CLE_PRODUIT_CONS_OPERATION);
        return new ResponseEntity<>(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(response),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{codeRefPoint}/{codeSourceDonnee}/{codeProduit}/{noCle}/{dateEnr}")
    @Operation(summary = "Supprimer une association point produit")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_ACCEPTED, description = APIConstant.HTTP_CODE_ACCEPTED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_NOT_FOUND, description = APIConstant.HTTP_CODE_NOT_FOUND_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> delete(
            @PathVariable("codeRefPoint") String codeRefPoint,
            @PathVariable("codeSourceDonnee") String codeSourceDonnee,
            @PathVariable("codeProduit") String codeProduit,
            @PathVariable("noCle") String noCle,
            @Parameter(
                    required = true, schema = @Schema(format = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"),
                    example = "2019-03-27T16:35:40.000Z"
            ) @PathVariable("dateEnr") @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) OffsetDateTime dateEnr
    ) throws NotFoundException, ValidationException, WebClientException {
        cleProduitConsService.delete(codeRefPoint, codeSourceDonnee, codeProduit, noCle, dateEnr);
        cleProduitConsService.synchronize();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_CLE_PRODUIT_CONS_OPERATION);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
