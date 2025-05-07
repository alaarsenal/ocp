package ca.qc.hydro.epd.controller;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
import ca.qc.hydro.epd.domain.BornePuis;
import ca.qc.hydro.epd.dto.BornePuisCreateDto;
import ca.qc.hydro.epd.dto.BornePuisDto;
import ca.qc.hydro.epd.dto.BornePuisSearchCriteriaDto;
import ca.qc.hydro.epd.dto.BornePuisUpdateDto;
import ca.qc.hydro.epd.dto.PageRequestDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.BornePuisMapper;
import ca.qc.hydro.epd.service.BornePuisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = BornePuisController.CONTEXT_V1_BORNE_PUIS)
@Tag(
        name = "BornePuisController",
        description = "Opérations permettant la gestion des données des bornes puissance par point pour l'application PD Calcul."
)
public class BornePuisController {

    public static final String CONTEXT_V1_BORNE_PUIS = APIConstant.CONTEXT_V1 + "borne-puis";

    private final BornePuisService bornePuisService;
    private final BornePuisMapper bornePuisMapper;

    @GetMapping
    @Operation(summary = "Recherche d'une page de bornes puissance par groupements, points, codes/types borne & numéros de mois.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Page<BornePuisDto>>> search(
            @RequestParam int page, @RequestParam int size,
            @RequestParam(required = false) String sortField, @RequestParam(required = false) Sort.Direction sortOrder,
            @Parameter(
                    name = "codesGrp", description = "Codes de groupement",
                    example = "BQ_SEUL"
            ) @RequestParam(required = false) List<String> codesGrp,
            @Parameter(
                    name = "codesRefPoints", description = "Codes Ref points pour lesquels on veut récupérer la borne de puissance",
                    example = "BQ"
            ) @RequestParam(required = false) List<String> codesRefPoints,
            @Parameter(
                    name = "codesBornes", description = "Codes des bornes de puissance",
                    example = "CONS"
            ) @RequestParam(required = false) List<String> codesBornes,
            @Parameter(
                    name = "typesBornes", description = "Types des bornes de puissance",
                    example = "MIN"
            ) @RequestParam(required = false) List<String> typesBornes,
            @Parameter(name = "noMois", description = "Liste des mois (1 à 12) pour lesquels on veut récupérer la borne de puissance") @RequestParam(
                    required = false
            ) List<Integer> noMois
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(bornePuisService
                .search(PageRequestDto.builder().page(page).size(size).sortField(sortField).sortOrder(sortOrder).build(),
                        codesGrp, codesRefPoints, codesBornes, typesBornes, noMois
                ).map(bornePuisMapper::toDto)));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de bornes puissance point par code point, code/type borne & numéro de mois.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<BornePuisDto>>> search(
            @Parameter(
                    name = "codeRefPoint", description = "Code Ref point pour lequel on veut récupérer la borne puissance",
                    example = "BQ"
            ) @RequestParam(required = false) String codeRefPoint,
            @Parameter(
                    name = "codeBorne", description = "Code de la borne puissance",
                    example = "CONS"
            ) @RequestParam(required = false) String codeBorne,
            @Parameter(
                    name = "typeBorne", description = "Type de la borne puissance",
                    example = "MIN"
            ) @RequestParam(required = false) String typeBorne,
            @Parameter(name = "noMois", description = "Liste des mois (1 à 12) pour lesquels on veut récupérer la borne puissance") @RequestParam(
                    required = false
            ) List<Integer> noMois
    ) {
        BornePuisSearchCriteriaDto searchCriteria = BornePuisSearchCriteriaDto.builder().codesRefPoints(Collections.singletonList(codeRefPoint))
                .codesBornes(Collections.singletonList(codeBorne)).typesBornes(Collections.singletonList(typeBorne)).noMois(noMois).build();
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(bornePuisMapper.toDtoList(bornePuisService.search(searchCriteria))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Visualiser une borne de puissance")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<BornePuisDto>> getBornePuis(@PathVariable("id") Long id) throws NotFoundException {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(bornePuisMapper.toDto(bornePuisService.getOne(id))));
    }

    @PostMapping
    @Operation(summary = "Création d'une borne de puissance")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_CREATED, description = APIConstant.HTTP_CODE_CREATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<BornePuisDto>> create(
            @Parameter(
                    name = "codesRefPoints", required = true, description = "Codes points pour lesquels on veut créer la borne de puissance",
                    example = "BQ"
            ) @RequestParam(name = "codesRefPoints") List<String> codesRefPoints,
            @Parameter(
                    name = "noMoisList", required = true, description = "Mois pour lesquels on veut créer la borne de puissance",
                    example = "[1,2,3]"
            ) @RequestParam(name = "noMoisList") List<Integer> noMoisList,
            @Valid @RequestBody BornePuisCreateDto dto
    ) throws ValidationException {
        BornePuis bornePuis = bornePuisMapper.createDtoToEntity(dto);

        final var response = bornePuisMapper.toDto(bornePuisService.create(bornePuis, codesRefPoints, noMoisList));

        bornePuisService.synchronize();

        return new ResponseEntity<>(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modification d'une borne de puissance")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UPDATED, description = APIConstant.HTTP_CODE_UPDATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<BornePuisDto>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody BornePuisUpdateDto dto
    ) throws ValidationException, NotFoundException {
        BornePuis bornePuis = bornePuisMapper.updateDtoToEntity(dto);
        bornePuis.setId(id);

        final var response = bornePuisMapper.toDto(bornePuisService.update(id, bornePuis));

        bornePuisService.synchronize();

        return new ResponseEntity<>(
                new ca.qc.hydro.epd.apierror.ApiResponse<>(response),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Suppression d'une borne de puissance")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_ACCEPTED, description = APIConstant.HTTP_CODE_ACCEPTED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> delete(@PathVariable("id") Long id)
            throws ValidationException, NotFoundException {
        bornePuisService.delete(id);

        bornePuisService.synchronize();

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
