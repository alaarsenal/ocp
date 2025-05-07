package ca.qc.hydro.epd.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.ConsbrutDto;
import ca.qc.hydro.epd.dto.PageDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.RedshiftQueryException;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.service.HelloRedshiftService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = HelloRedshiftController.CONTEXT_V1_REDSHIFT)
@Tag(name = "HelloRedshiftController", description = "Test de la communication avec Redshift")
public class HelloRedshiftController {

    static final String CONTEXT_V1_REDSHIFT = APIConstant.CONTEXT_V1 + "redshift";

    private final HelloRedshiftService helloRedshiftService;

    @GetMapping("hello")
    public ResponseEntity<ApiResponse<List<ConsbrutDto>>> helloRedshift(
            @RequestParam LocalDateTime dateDebut,
            @RequestParam LocalDateTime dateFin,
            @RequestParam String codeRefPoint
    ) throws RedshiftQueryException, ValidationException, NotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(helloRedshiftService.getConsbruts(dateDebut, dateFin, codeRefPoint)));
    }

    @GetMapping("hello/page")
    public ResponseEntity<ApiResponse<Page<ConsbrutDto>>> helloRedshift(
            @RequestParam LocalDateTime dateDebut,
            @RequestParam LocalDateTime dateFin,
            @RequestParam String codeRefPoint,
            @RequestParam int page,
            @RequestParam int size
    ) throws RedshiftQueryException, ValidationException, NotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(helloRedshiftService.getConsbruts(dateDebut, dateFin, codeRefPoint, PageDto.builder().page(page).size(size).build())));
    }

}
