package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.TypePrevision;
import ca.qc.hydro.epd.service.TypePrevisionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = TypePrevisionController.CONTEXT_V1_TYPE_PREV)
@Tag(name = "TypePrevisionController", description = "TypePrevisionController")
public class TypePrevisionController {

    public static final String CONTEXT_V1_TYPE_PREV = APIConstant.CONTEXT_V1 + "types-prev";

    private final TypePrevisionService typePrevisionService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les types de prévisions.")
    public ResponseEntity<ApiResponse<List<TypePrevision>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(typePrevisionService.getAll()));
    }
}
