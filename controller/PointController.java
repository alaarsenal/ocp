package ca.qc.hydro.epd.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.PointDto;
import ca.qc.hydro.epd.dto.PointSearchDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.mapper.PointMapper;
import ca.qc.hydro.epd.service.PointService;
import ca.qc.hydro.epd.service.wsclient.PdCalculJolokiaService;
import ca.qc.hydro.epd.utils.Constantes;

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
@RequestMapping(value = PointController.CONTEXT_V1_POINT)
@Tag(
        name = "PointController",
        description = "Opérations permettant la gestion des données des points & des groupements de points pour l'application PD Calcul."
)
public class PointController {

    public static final String CONTEXT_V1_POINT = APIConstant.CONTEXT_V1 + "points";

    private final PointService pointService;
    private final PointMapper pointMapper;
    private final PdCalculJolokiaService pdCalculJolokiaService;

    @GetMapping("/pilotage")
    @Operation(summary = "Visualiser les points de prévisions.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<PointSearchDto>>> getPoints() {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(pointService.getPoints()));
    }

    @GetMapping
    @Operation(description = "Visualiser la liste de TOUS les points présents à l'intérieur d'un ou de plusieurs groupement(s).")
    @Parameter(name = "codesGrp", description = "Codes des groupements points pour lesquels on veut récupérer la liste des points")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<PointDto>>> getPoints(
            @RequestParam(value = "codesGrp", required = false) List<String> codesGrp
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(pointMapper.toDtoList(pointService.getPointsByGrp(codesGrp))));
    }

    @GetMapping("/{codeRefPoint}")
    @Operation(description = "Visualiser un point par son codeRef")
    @Parameter(name = "codeRefPoint", description = "Code ref du point")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<PointDto>> getPoint(@PathVariable("codeRefPoint") String codeRefPoint) throws NotFoundException {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(pointMapper.toDto(pointService.getOneByCodeRef(codeRefPoint))));
    }

    @PostMapping
    @Operation(description = "Création d'un point")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_CREATED, description = APIConstant.HTTP_CODE_CREATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<PointDto>> create(@Valid @RequestBody PointDto dto) throws ValidationException, ExecutionException, InterruptedException, WebClientException {
        Point point = pointMapper.toEntity(dto);
        Point createdPoint = pointService.create(point);
        final var response = pointMapper.toDto(createdPoint);
        pointService.synchronize();
        pointService.clearCacheAndSendNotification();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_POINTS_OPERATION);
        return new ResponseEntity<>(new ca.qc.hydro.epd.apierror.ApiResponse<>(response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(description = "Modification d'un point")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UPDATED, description = APIConstant.HTTP_CODE_UPDATED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<PointDto>> update(@PathVariable("id") Long id, @Valid @RequestBody PointDto dto) throws ValidationException, ExecutionException, InterruptedException, WebClientException {
        Point point = pointMapper.toEntity(dto);
        point.setId(id);
        Point updatedPoint = pointService.update(point);
        final var response = pointMapper.toDto(updatedPoint);
        pointService.synchronize();
        pointService.clearCacheAndSendNotification();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_POINTS_OPERATION);
        return new ResponseEntity<>(new ca.qc.hydro.epd.apierror.ApiResponse<>(response), HttpStatus.OK);
    }

    @DeleteMapping("/{codeRefPoint}")
    @Operation(description = "Suppression d'un point")
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_ACCEPTED, description = APIConstant.HTTP_CODE_ACCEPTED_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<String>> delete(@PathVariable("codeRefPoint") String codeRefPoint) throws ValidationException, NotFoundException, ExecutionException, InterruptedException, WebClientException {
        pointService.delete(codeRefPoint);
        pointService.synchronize();
        pointService.clearCacheAndSendNotification();
        pdCalculJolokiaService.rafraichirCache(Constantes.PD_CALCUL_CACHE_POINTS_OPERATION);
        return new ResponseEntity<>(new ca.qc.hydro.epd.apierror.ApiResponse<>(codeRefPoint), HttpStatus.ACCEPTED);
    }
}
