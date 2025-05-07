package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.domain.TypeDonInd;
import ca.qc.hydro.epd.service.TypeDonIndService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = TypeDonIndController.CONTEXT_V1_TYPE_DON_IND)
@Tag(name = "TypeDonIndController", description = "TypeDonIndController")
public class TypeDonIndController {

    public static final String CONTEXT_V1_TYPE_DON_IND = APIConstant.CONTEXT_V1 + "types-don-ind";

    private final TypeDonIndService typeDonIndService;

    @GetMapping
    @Operation(summary = "Visualiser la liste de TOUS les types de données pour les clients industriels.")
    public ResponseEntity<ApiResponse<List<TypeDonInd>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(typeDonIndService.getAll()));
    }
}
