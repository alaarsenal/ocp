package ca.qc.hydro.epd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.TypePointDto;
import ca.qc.hydro.epd.mapper.TypePointMapper;
import ca.qc.hydro.epd.service.TypePointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = TypePointController.CONTEXT_V1_TYPE_POINT)
@Tag(name = "TypePointController", description = "TypePointController")
public class TypePointController {

    public static final String CONTEXT_V1_TYPE_POINT = APIConstant.CONTEXT_V1 + "types-points";

    private final TypePointService typePointService;
    private final TypePointMapper typePointMapper;

    @GetMapping
    @Operation(summary = "Visualiser la liste de tous les types de points.")
    public ResponseEntity<ApiResponse<List<TypePointDto>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(typePointMapper.toDtoList(typePointService.getAll())));
    }

}
