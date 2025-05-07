package ca.qc.hydro.epd.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.hydro.epd.APIConstant;
import ca.qc.hydro.epd.apierror.ApiMessage;
import ca.qc.hydro.epd.apierror.ApiMessageLevel;
import ca.qc.hydro.epd.apierror.ApiResponse;
import ca.qc.hydro.epd.dto.MeilleurePrevisionResponseDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.service.MeilleurePrevisionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = MeilleurePrevisionController.CONTEXT_V1_MEILLEURE_PREVISION)
@Tag(name = "MeilleurePrevisionController", description = "MeilleurePrevisionController")
public class MeilleurePrevisionController {

    public static final String CONTEXT_V1_MEILLEURE_PREVISION = APIConstant.CONTEXT_V1 + "meilleures-previsions";

    private final MeilleurePrevisionService meilleurePrevisionService;

    @GetMapping
    @Operation(summary = "Retourner les prévisions publiées pour toute période de temps.")
    public ResponseEntity<ApiResponse<MeilleurePrevisionResponseDto>> getPrevisions(
            @RequestParam String codeRefPoint,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFin
    ) throws WebClientException, NotFoundException {
        ApiResponse<MeilleurePrevisionResponseDto> response = new ApiResponse<>(meilleurePrevisionService.getPrevisions(dateDebut, dateFin, codeRefPoint));
        response.setMessages(getApiMessages(response.getData()));
        return ResponseEntity.ok(response);
    }

    private List<ApiMessage> getApiMessages(MeilleurePrevisionResponseDto meilleurePrevisionResponseDto) {
        List<ApiMessage> apiMessages = new ArrayList<>();

        if (Objects.nonNull(meilleurePrevisionResponseDto) && Objects.nonNull(meilleurePrevisionResponseDto.getPrevisionCtMtHorResponse())) {
            var messagesRetour = meilleurePrevisionResponseDto.getPrevisionCtMtHorResponse().getMessagesRetour();
            messagesRetour.forEach(
                    messageRetour -> {
                        var apiMessage = new ApiMessage();
                        apiMessage.setLevel(ApiMessageLevel.fromCode(messageRetour.getTypeMessage()));
                        apiMessage.setCode(messageRetour.getNoMessage());
                        apiMessage.setMessage(messageRetour.getTexteMessage());
                        apiMessages.add(apiMessage);
                    });
        }

        return apiMessages;
    }

}
