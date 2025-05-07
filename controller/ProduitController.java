package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.ProduitDto;
import ca.qc.hydro.epd.mapper.ProduitMapper;
import ca.qc.hydro.epd.service.ProduitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-25
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = ProduitController.CONTEXT_V1_PRODUIT)
@Tag(name = "ProduitController", description = "ProduitController")
public class ProduitController {

    public static final String CONTEXT_V1_PRODUIT = APIConstant.CONTEXT_V1 + "produits";

    private final ProduitService produitService;
    private final ProduitMapper produitMapper;

    @GetMapping
    @Operation(summary = "Visualiser la liste de tous les produits.")
    public ResponseEntity<ApiResponse<List<ProduitDto>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(produitMapper.toDtoList(produitService.getAll())));
    }

}
