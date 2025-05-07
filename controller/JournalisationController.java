package ca.qc.hydro.epd.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.Journalisation;
import ca.qc.hydro.epd.dto.ActionAttributDto;
import ca.qc.hydro.epd.dto.ActionJournalDto;
import ca.qc.hydro.epd.dto.CategorieJournalDto;
import ca.qc.hydro.epd.dto.ConseillerDto;
import ca.qc.hydro.epd.dto.JournalRequestDto;
import ca.qc.hydro.epd.dto.JournalisationDto;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.mapper.ActionJournalMapper;
import ca.qc.hydro.epd.mapper.AttributJournalMapper;
import ca.qc.hydro.epd.mapper.CategorieJournalMapper;
import ca.qc.hydro.epd.mapper.JournalisationMapper;
import ca.qc.hydro.epd.service.ActionJournalService;
import ca.qc.hydro.epd.service.AttributJournalService;
import ca.qc.hydro.epd.service.CategorieJournalService;
import ca.qc.hydro.epd.service.JournalisationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = JournalisationController.CONTEXT_V1)
@Tag(name = "Journalisation", description = "Journalisation")
public class JournalisationController {

    public static final String CONTEXT_V1 = APIConstant.CONTEXT_V1 + "journalisation";

    private final JournalisationService journalisationService;
    private final CategorieJournalService categorieJournalService;
    private final ActionJournalService actionJournalService;
    private final AttributJournalService attributJournalService;

    private final JournalisationMapper journalisationMapper;
    private final CategorieJournalMapper categorieJournalMapper;
    private final ActionJournalMapper actionJournalMapper;
    private final AttributJournalMapper attributJournalMapper;

    @PostMapping("/journaux")
    @Operation(summary = "Rechercher une Page de journaux en se basant sur les critères en paramètres.")
    public ResponseEntity<ApiResponse<Page<JournalisationDto>>> getJournalisations(@Valid @RequestBody JournalRequestDto dto) {
        return ResponseEntity.ok(new ApiResponse<>(journalisationService.getPage(dto).map(journalisationMapper::toDto)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Extraire la liste des categories pour la journalisation.")
    public ResponseEntity<ApiResponse<List<CategorieJournalDto>>> getCategories() {
        return ResponseEntity.ok(new ApiResponse<>(categorieJournalMapper.toDtoList(categorieJournalService.getAll())));
    }

    @GetMapping("/actions")
    @Operation(summary = "Extraire la liste des actions pour la journalisation.")
    public ResponseEntity<ApiResponse<List<ActionJournalDto>>> getActions() {
        return ResponseEntity.ok(new ApiResponse<>(actionJournalMapper.toDtoList(actionJournalService.getAll())));
    }

    @GetMapping("/conseillers")
    @Operation(summary = "Extraire la liste des conseillers.")
    public ResponseEntity<ApiResponse<List<ConseillerDto>>> getConseillers() {
        return ResponseEntity.ok(new ApiResponse<>(journalisationService.getConseillers()));
    }

    @PostMapping("/journaux/create")
    @Operation(summary = "Création d'une ou plus journalisations.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = APIConstant.HTTP_CODE_CREATED, description = APIConstant.HTTP_CODE_CREATED_MESSAGE)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = APIConstant.HTTP_CODE_BAD_REQUEST, description = APIConstant.HTTP_CODE_BAD_REQUEST_MESSAGE)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY, description = APIConstant.HTTP_CODE_UNPROCESSABLE_ENTITY_MESSAGE)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = APIConstant.HTTP_CODE_UNAUTHORIZED, description = APIConstant.HTTP_CODE_UNAUTHORIZED_MESSAGE)
    public ResponseEntity<ApiResponse<List<JournalisationDto>>> createJournaux(@Valid @RequestBody List<JournalisationDto> dtos) throws ValidationException {
        List<Journalisation> journalisations = journalisationMapper.createDtoToEntityList(dtos);
        return new ResponseEntity<>(
                new ApiResponse<>(journalisationMapper.toDtoList(journalisationService.create(journalisations))), HttpStatus.CREATED);
    }

    @GetMapping("/attributs")
    @Operation(summary = "Extraire la liste des attributs")
    public ResponseEntity<ApiResponse<List<ActionAttributDto>>> getAttributs() {
        return ResponseEntity.ok(new ApiResponse<>(attributJournalMapper.toDtoList((attributJournalService.getAllActionsAttributs()))));
    }

    @GetMapping("/journaux/{parentId}/enfants")
    @Operation(summary = "Extraire la liste des enfants d'une journalisation avec le parentId.")
    public ResponseEntity<ApiResponse<List<JournalisationDto>>> getJournauxEnfants(@PathVariable Long parentId) throws ValidationException {
        return ResponseEntity.ok(new ApiResponse<>(journalisationMapper.toDtoList((journalisationService.getJournalisationsEnfantsByParentId(parentId)))));
    }

}
