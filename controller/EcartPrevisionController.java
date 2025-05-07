package ca.qc.hydro.epd.controller;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.ConsommationsRequestDto;
import ca.qc.hydro.epd.dto.DatesPrevisionDto;
import ca.qc.hydro.epd.dto.PrevisionDonnesConsoReelDto;
import ca.qc.hydro.epd.dto.PrevisionDonnesDto;
import ca.qc.hydro.epd.dto.PrevisionRequestDto;
import ca.qc.hydro.epd.service.EcartPrevisionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-06-13
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = EcartPrevisionController.CONTEXT_V1_ECART_PREVISION)
@Tag(name = "EcartPrevisionController", description = "EcartPrevisionController")
public class EcartPrevisionController {

    public static final String CONTEXT_V1_ECART_PREVISION = APIConstant.CONTEXT_V1 + "ecarts-previsions";

    private final EcartPrevisionService ecartPrevisionService;

    @GetMapping("/previsions")
    @Operation(summary = "Récupérer les prévisions")
    public ResponseEntity<ApiResponse<PrevisionDonnesDto>> getPrevisions(@Valid PrevisionRequestDto dto) {
        DatesPrevisionDto datesPrevision = DatesPrevisionDto.builder()
                .dateDebut(dto.getDateDebut().toLocalDateTime())
                .dateFin(dto.getDateFin().toLocalDateTime())
                .dateReference(Objects.nonNull(dto.getDateReference()) ? dto.getDateReference().toLocalDateTime() : LocalDateTime.now())
                .build();
        PrevisionDonnesDto result = ecartPrevisionService.getPrevisions(
                datesPrevision,
                dto.getProjection(),
                dto.getTempsPrevision(),
                dto.getCodesRefPoints(),
                dto.getModeles(),
                dto.getFonctions(),
                dto.getCodesProduitPrev(),
                dto.getCodesTypePrevision(),
                dto.getEtiquette(),
                dto.getTypePrevision()
        );



        return ResponseEntity.ok(new ApiResponse<>(result));
    }

    @GetMapping("/consommations")
    @Operation(summary = "Récupérer les consommations")
    public ResponseEntity<ApiResponse<PrevisionDonnesConsoReelDto>> getConsommations(@Valid ConsommationsRequestDto dto) {

        final var response = ecartPrevisionService.getConsommations(
                dto.getDateDebut().toLocalDateTime(), dto.getDateFin().toLocalDateTime(),
                dto.getCodesRefPoints()
        );

        return ResponseEntity.ok(new ApiResponse<>(response));
    }

}
