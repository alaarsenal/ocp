package ca.qc.hydro.epd.controller;

import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.context.request.WebRequest;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.domain.VersionParametres;
import ca.qc.hydro.epd.dto.VersionParametresDto;
import ca.qc.hydro.epd.dto.VersionParametresResponseDto;
import ca.qc.hydro.epd.dto.VpDonneeParamUpdateDto;
import ca.qc.hydro.epd.dto.VpMiseEnExploitationDto;
import ca.qc.hydro.epd.dto.VpMiseEnExploitationResponseDto;
import ca.qc.hydro.epd.dto.VpSearchCriteriaDto;
import ca.qc.hydro.epd.exception.ConcurrentEditionException;
import ca.qc.hydro.epd.exception.EtagInvalidException;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.mapper.VersionParametresMapper;
import ca.qc.hydro.epd.service.VersionParametresService;
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
@RequestMapping(value = VersionParametresController.CONTEXT_V1_VP)
@Tag(name = "VersionParametresController", description = "VersionParametresController")
public class VersionParametresController {

    public static final String CONTEXT_V1_VP = APIConstant.CONTEXT_V1 + "vp";

    private final VersionParametresMapper versionParametresMapper;
    private final VersionParametresService versionParametresService;

    private final MessageSource messageSource;

    @Operation(summary = "Copie d'une version de paramètres.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(
                                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"6000\",\"texteMessage\":\"Impossible de supprimer une version de paramètres 0 (en exploitation).\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                                    )},
                                    mediaType = "application/json"
                            )
                    )}
    )
    @PostMapping("/copie")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<VersionParametresResponseDto>>> copy(@Valid @RequestBody VersionParametresDto copyDto) throws NotFoundException, ValidationException {
        List<VersionParametresResponseDto> vpList = versionParametresMapper.toDtoList(versionParametresService.copy(copyDto), copyDto);
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, vpList));
    }

    @Operation(summary = "Supprimer une ou plusieurs version(s) de paramètres.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "422", description = "Erreur de validation lors de l'application des règles d'affaires",
                            content = @Content(
                                    examples = {@ExampleObject(
                                            value = "{\"donnees\":{},\"messagesRetour\":[{\"noMessage\":\"6000\",\"texteMessage\":\"Impossible de supprimer une version de paramètres 0 (en exploitation).\",\"typeMessage\":\"ERREUR\"}],\"statut\":\"ECHEC\"}"
                                    )},
                                    mediaType = "application/json"
                            )
                    )}
    )
    @PostMapping("/batch-delete")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<Void>> delete(@RequestBody @Valid List<VersionParametresDto> vps) throws NotFoundException, ValidationException {
        versionParametresService.delete(versionParametresMapper.toVersionParametres(vps));
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(null));
    }

    @GetMapping
    @Operation(summary = "Visualiser les détails d'une version de paramètres.")
    @Parameter(name = "vpId", required = true, description = "Id de la version de paramètres")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<VersionParametresResponseDto>> get(@RequestParam("vpId") Long vpId) throws NotFoundException {
        VersionParametres vp = versionParametresService.get(vpId);
        return ResponseEntity.ok().eTag(Long.toString(vp.getDateMaj().toEpochSecond(ZoneOffset.UTC))).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS,
                versionParametresMapper.toDto(vp)
        ));
    }

    @PostMapping("/mise-en-exploitation")
    @Operation(summary = "Mise en exploitation d'une version de paramètres.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<VpMiseEnExploitationResponseDto>> mettreEnExploitation(@RequestBody @Valid VpMiseEnExploitationDto dto)
            throws NotFoundException, ValidationException, WebClientException {
        VpMiseEnExploitationResponseDto response = versionParametresService.mettreEnExploitation(dto);
        versionParametresService.synchronize();
        return ResponseEntity.ok().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS, response));
    }

    @PatchMapping
    @Operation(summary = "Modifier la description d'une version de paramètres.")
    @Parameter(name = "If-Match", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER, description = "Numéro de version pour gérer la concurrence", example = "\"1569884789000\"")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<VersionParametresResponseDto>> update(@Parameter(hidden = true) WebRequest request, @RequestBody VersionParametresDto updatedVp)
            throws NotFoundException, ConcurrentEditionException, ValidationException {

        String ifMatchValue = request.getHeader("If-Match");
        if (StringUtils.isEmpty(ifMatchValue)) {
            log.warn(ApiMessageCode.HTTP_HEADER_IF_MATCH_MISSING.getDefaultMessage());
            return ResponseEntity.badRequest().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                    ca.qc.hydro.epd.apierror.ApiResponse.Status.FAILURE,
                    ApiMessageFactory.getError(ApiMessageCode.HTTP_HEADER_IF_MATCH_MISSING, messageSource)
            ));
        }

        updatedVp.setDateMaj(ifMatchValue);
        VersionParametres updatedVersParam = versionParametresService.update(updatedVp);
        return ResponseEntity.ok().eTag(Long.toString(updatedVersParam.getDateMaj().toEpochSecond(ZoneOffset.UTC))).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS,
                versionParametresMapper.toDto(updatedVersParam)
        ));
    }

    @PatchMapping("/donnees-params")
    @Operation(summary = "Modifier les valeurs des paramètres associés à une version.")
    @Parameter(name = "If-Match", schema = @Schema(type = "string"), in = ParameterIn.HEADER, description = "Numéro de version pour gérer la concurrence", example = "\"1569884789000\"")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<VersionParametresResponseDto>> updateParams(
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.IF_MATCH) final String ifMatch,
            @Valid @RequestBody VpDonneeParamUpdateDto updatedVpDto
    ) throws EtagInvalidException, NotFoundException, ConcurrentEditionException, SQLException, ValidationException {

        ca.qc.hydro.epd.apierror.ApiResponse<Void> response = EtagUtils.verifyIfMatchIsPresent(ifMatch, messageSource);
        if (response != null) {
            return ResponseEntity.badRequest().body(new ca.qc.hydro.epd.apierror.ApiResponse<>(response.getStatus(), response.getMessages()));
        }
        updatedVpDto.setVersion(EtagUtils.convertEtagToTime(ifMatch, messageSource));

        VersionParametres updatedVp = versionParametresService.updateParams(updatedVpDto);
        return ResponseEntity.ok().eTag(Long.toString(updatedVp.getDateMaj().toEpochSecond(ZoneOffset.UTC))).body(new ca.qc.hydro.epd.apierror.ApiResponse<>(
                ca.qc.hydro.epd.apierror.ApiResponse.Status.SUCCESS,
                versionParametresMapper.toDto(updatedVp)
        ));
    }

    @GetMapping("/annees")
    @Operation(summary = "Visualiser la liste des années distinctes, pour un ou plusieurs point(s) spécifique(s), pour lesquelles il existe au moins une version de paramètres.")
    @Parameter(name = "codesRefPoints", description = "Codes Ref des points pour lesquels on veut récupérer la liste des années")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Integer>>> getAnneesByPoints(@RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresService.getAnneesByPoints(codesRefPoints)));
    }

    @GetMapping("/modeles")
    @Operation(summary = "Visualiser la liste des modèles distincts, pour une combinaison de point(s)/année(s), pour lesquelles il existe au moins une version de paramètres.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Modele>>> getModelesByPoint(
            @RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints,
            @RequestParam(value = "annees", required = false) List<Integer> annees
    ) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresService.getModelesByPointsAndAnnees(codesRefPoints, annees)));
    }

    @GetMapping("/saisons")
    @Operation(summary = "Visualiser la liste des saisons distinctes, pour un ou plusieurs point(s) spécifique(s), pour lesquelles il existe au moins une version de paramètres.")
    @Parameter(name = "codesRefPoints", description = "Codes Ref des points pour lesquels on veut récupérer la liste des saisons")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Character>>> getSaisonsByPoint(@RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresService.getSaisonsByPoints(codesRefPoints)));
    }

    @GetMapping("/versions")
    @Operation(summary = "Visualiser la liste des numéros de versions, pour un ou plusieurs point(s) spécifique(s), pour lesquelles il existe au moins une version de paramètres.")
    @Parameter(name = "codesRefPoints", description = "Codes Ref des points pour lesquels on veut récupérer la liste des numéros de versions")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<Integer>>> getVersionsByPoint(@RequestParam(value = "codesRefPoints", required = false) List<String> codesRefPoints) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresService.getVersionsByPoints(codesRefPoints)));
    }

    @GetMapping("/recherche-vp-distinctes")
    @Operation(summary = "Recherche de VP distinctes (point, modèle, année, saison & numéro de VP) selon les critères de sélection fournis.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<VersionParametresResponseDto>>> searchDistinct(VpSearchCriteriaDto searchCriteria) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresMapper.toDtoList(versionParametresService.searchDistinct(searchCriteria))));
    }

    @GetMapping("/recherche-vp")
    @Operation(summary = "Recherche de VP selon les critères de sélection fournis.")
    public ResponseEntity<ca.qc.hydro.epd.apierror.ApiResponse<List<VersionParametresResponseDto>>> search(VpSearchCriteriaDto searchCriteria) {
        return ResponseEntity.ok(new ca.qc.hydro.epd.apierror.ApiResponse<>(versionParametresMapper.toDtoList(versionParametresService.search(searchCriteria))));
    }
}
