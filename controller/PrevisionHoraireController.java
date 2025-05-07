package ca.qc.hydro.epd.controller;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.PrevisionBqDonnees;
import ca.qc.hydro.epd.dto.PrevisionHoraireDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.service.PrevisionHoraireService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2021-12-06
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = PrevisionHoraireController.CONTEXT_V1_PREVISION_HORAIRE)
@Tag(name = "PrevisionHoraireController", description = "PrevisionHoraireController")
public class PrevisionHoraireController {

    public static final String CONTEXT_V1_PREVISION_HORAIRE = APIConstant.CONTEXT_V1 + "previsions-horaires";

    private final PrevisionHoraireService previsionHoraireService;

    @GetMapping("/periode")
    @Operation(summary = "Récupérer les prévisions horaires 3 jours")
    @Parameter(name = "dateRef", description = "Date de référence", schema = @Schema(format = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"), example = "2021-12-20T05:01:00.000Z")
    public ResponseEntity<ApiResponse<PrevisionBqDonnees>> getPrevisionsHorairesPeriode(
            @RequestParam(value = "dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateRef,
            @RequestParam(value = "projection") Integer projection,
            @RequestParam(value = "nombreHeures") Integer nombreHeures,
            @RequestParam(value = "codeRefPoint") String codeRefPoint
    ) throws ExecutionException, InterruptedException {
        PrevisionBqDonnees previsionBqDonnees = null;
        try {
            previsionBqDonnees = previsionHoraireService.getPrevisionsHorairesPeriode(dateRef.toLocalDateTime(), projection, nombreHeures, codeRefPoint);
        } catch (NotFoundException e) {
            new ApiResponse<>(ApiResponse.Status.FAILURE, e.getApiMessages());
        }
        return ResponseEntity.ok(new ApiResponse<>(previsionBqDonnees));
    }

    @GetMapping("/calcul-flexible")
    @Operation(summary = "Chercher un calcul précédent afin d'éviter d'afficher des cases vides dans l'écran Prévision BQ")
    @Parameter(name = "datePrevue", description = "Date de prévision", schema = @Schema(format = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX"), example = "2023-04-10T08:00:00.000Z")
    public ResponseEntity<ApiResponse<PrevisionHoraireDto>> getPrevisionPourDatePrevueAvecTolerance(
            @RequestParam(value = "datePrevue") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime datePrevue,
            @RequestParam(value = "projection") Integer projection,
            @RequestParam(value = "codeRefPoint") String codeRefPoint
    ) throws ExecutionException, InterruptedException, NotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(previsionHoraireService.getPrevisionPourDatePrevueAvecTolerance(datePrevue.toLocalDateTime(), projection, codeRefPoint)));
    }

}
