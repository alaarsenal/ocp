package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.TypeCalc;
import ca.qc.hydro.epd.dto.MessageDto;
import ca.qc.hydro.epd.dto.NombreJourValidationVersionParametreCalendrierCalculDto;
import ca.qc.hydro.epd.dto.PointModelDto;
import ca.qc.hydro.epd.dto.ValidePointModeleDto;
import ca.qc.hydro.epd.enums.EDashboardSecurite;
import ca.qc.hydro.epd.enums.ETypeCalcul;
import ca.qc.hydro.epd.enums.ETypeCalculList;
import ca.qc.hydro.epd.repository.AssoVersionParametresRepository;
import ca.qc.hydro.epd.repository.PointModeleFonctionRepository;
import ca.qc.hydro.epd.repository.TypeCalcRepository;
import ca.qc.hydro.epd.service.dashboardmessage.CumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer;
import ca.qc.hydro.epd.service.dashboardmessage.GroupementVersionParametreCalendrierCalculAbsentConsumer;
import ca.qc.hydro.epd.utils.Constantes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DashboardMessagesService {

    private final MessageService messageService;
    private final AssoVersionParametresRepository assoVersionParametresRepository;
    private final PointModeleFonctionRepository pointModeleFonctionRepository;
    private final TypeCalcRepository typeCalcRepository;

    public List<MessageDto> obtenirMessages(EDashboardSecurite dashboardSecurite) {
        List<MessageDto> messages = new ArrayList<>();

        if (EDashboardSecurite.CALENDRIER_VP.equals(dashboardSecurite)) {
            messages.addAll(validerVersionParametreCalendrierCalculAbsent());
        }
        return messages;
    }

    private List<MessageDto> validerVersionParametreCalendrierCalculAbsent() {
        final LocalDateTime actuelLocalDateTime = LocalDateTime.now();
        NombreJourValidationVersionParametreCalendrierCalculDto nombreJourValidation = obtenirNombreJourValidation();
        List<PointModelDto> actifPointModeles = pointModeleFonctionRepository.obtenirActifCodesPointModeleFunction(ETypeCalculList.CT.getCodes());
        List<ValidePointModeleDto> validePointModeles = actifPointModeles.stream().map(pointModeleFonction -> ValidePointModeleDto.builder()
                .codeRefPoint(pointModeleFonction.getCodeRefPoint())
                .codeModele(pointModeleFonction.getCodeModele()).build()).toList();
        List<PointModelDto> futureAssociationVersionParametres = assoVersionParametresRepository.futureAssociationVersionParametres(LocalDateTime.now().minusDays(nombreJourValidation.getAvant()));

        CumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer cumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer =
                new CumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer(validePointModeles);
        futureAssociationVersionParametres.forEach(cumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer);

        GroupementVersionParametreCalendrierCalculAbsentConsumer groupementVersionParametreCalendrierCalculAbsentConsumer =
                new GroupementVersionParametreCalendrierCalculAbsentConsumer(actuelLocalDateTime, nombreJourValidation);
        cumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer.cumulerPeriodeValidePointModeles().forEach(groupementVersionParametreCalendrierCalculAbsentConsumer);

        return messageService.versionParametreCalendrierCalculAbsent(groupementVersionParametreCalendrierCalculAbsentConsumer.groupementInvalidePointModeles(), nombreJourValidation.getLimiteAvertissement());
    }

    private NombreJourValidationVersionParametreCalendrierCalculDto obtenirNombreJourValidation() {
        NombreJourValidationVersionParametreCalendrierCalculDto nombreJourValidation = new NombreJourValidationVersionParametreCalendrierCalculDto();
        List<TypeCalc> typeCalcs = typeCalcRepository.findByCodeGrp(Constantes.CALCUL_CYCL);

        for (TypeCalc typeCalc : typeCalcs) {

            if (ETypeCalcul.CYCL.getCode().equals(typeCalc.getCodeTypePrev())) {
                nombreJourValidation.setLimiteAvertissement(typeCalc.getNbJoursApres());
            } else if (ETypeCalcul.PPCT.getCode().equals(typeCalc.getCodeTypePrev())) {
                nombreJourValidation.setAvant(typeCalc.getNbJoursAvant());
                nombreJourValidation.setApres(typeCalc.getNbJoursApres());
            }
        }
        return nombreJourValidation;
    }

}
