package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.apierror.ApiMessageLevel;
import ca.qc.hydro.epd.dto.CodeMessage;
import ca.qc.hydro.epd.dto.GroupementValidePointModelDto;
import ca.qc.hydro.epd.dto.MessageDto;
import ca.qc.hydro.epd.utils.DatePrevisionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageService {

    private final MessageSource messageSource;

    public List<MessageDto> versionParametreCalendrierCalculAbsent(List<GroupementValidePointModelDto> groupementInvalidePointModeles, int limiteAvertissement) {
        List<MessageDto> messages = new ArrayList<>();
        List<GroupementValidePointModelDto> modifieGroupementInvalidePointModeles = new ArrayList<>(groupementInvalidePointModeles);
        modifieGroupementInvalidePointModeles.sort(Comparator.comparing(GroupementValidePointModelDto::getCouvertureLocalDateTime));
        modifieGroupementInvalidePointModeles.forEach(r -> {
            r.setInvalidPointModeleDtos(new ArrayList<>(r.getInvalidPointModeleDtos()));
            r.getInvalidPointModeleDtos().sort((x, y) -> {
                int pointCompareTo = x.getCodeRefPoint().compareTo(y.getCodeRefPoint());

                if (pointCompareTo == 0) {
                    return x.getCodeModele().compareTo(y.getCodeModele());
                } else {
                    return pointCompareTo;
                }
            });
        });

        modifieGroupementInvalidePointModeles.forEach(r ->
                messages.addAll(MessageDtoFactory.getMessage(obtenirCodeMessage(r), obtenirApiMessageLevel(r.getJoursRestantCouverture(), limiteAvertissement),
                        // Example message: Pour les {0} derniers jours ({1}), {2} ne sont pas couverts par une version de paramètre en exploitation.
                        new Object[]{nombreJoursAffichageAbsolue(r), DatePrevisionUtil.affichageDateFormatter.format(r.getCouvertureLocalDateTime()), getPointModelMessage(r)}, messageSource
                )));
        return messages;
    }

    private ApiMessageLevel obtenirApiMessageLevel(long joursRestantCouverture, int limiteAvertissement) {

        if (joursRestantCouverture < limiteAvertissement) {
            return ApiMessageLevel.ERROR;
        }
        return ApiMessageLevel.WARNING;
    }

    private CodeMessage obtenirCodeMessage(GroupementValidePointModelDto r) {
        CodeMessage codeMessage = null;

        if (r.getJoursRestantCouverture() == 0) {

            if (r.getInvalidPointModeleDtos().size() == 1) {
                codeMessage = CodeMessage.AUJOURDHUI_SINGULIER_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            } else {
                codeMessage = CodeMessage.AUJOURDHUI_PLURIEL_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            }
        } else if (r.getJoursRestantCouverture() < 0) {

            if (r.getInvalidPointModeleDtos().size() == 1) {
                codeMessage = CodeMessage.PASSE_SINGULIER_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            } else {
                codeMessage = CodeMessage.PASSE_PLURIEL_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            }
        } else {
            if (r.getInvalidPointModeleDtos().size() == 1) {
                codeMessage = CodeMessage.FUTURE_SINGULIER_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            } else {
                codeMessage = CodeMessage.FUTURE_PLURIEL_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT;
            }
        }
        return codeMessage;
    }

    private String nombreJoursAffichageAbsolue(GroupementValidePointModelDto r) {
        long nombreJours = Math.abs(r.getJoursRestantCouverture());

        if (nombreJours == 0)
            return "aujourd'hui";
        return String.valueOf(nombreJours);
    }

    private String getPointModelMessage(GroupementValidePointModelDto groupementValidePointModelDto) {
        StringBuilder builder = new StringBuilder();
        groupementValidePointModelDto.getInvalidPointModeleDtos().forEach(g ->
                builder.append(MessageDtoFactory.getMessage(CodeMessage.POINT_MODELE_VERSION_PARAMETRES_CALENDRIER_CALCUL_ABSENT,
                        new Object[]{g.getCodeRefPoint(), g.getCodeModele()}, messageSource
                )));
        return builder.toString();
    }

}
