package ca.qc.hydro.epd.service.dashboardmessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import ca.qc.hydro.epd.dto.GroupementValidePointModelDto;
import ca.qc.hydro.epd.dto.NombreJourValidationVersionParametreCalendrierCalculDto;
import ca.qc.hydro.epd.dto.PointModelDto;
import ca.qc.hydro.epd.dto.ValidePointModeleDto;

public class GroupementVersionParametreCalendrierCalculAbsentConsumer implements Consumer<ValidePointModeleDto> {

    private final List<GroupementValidePointModelDto> groupementInvalidePointModeles = new ArrayList<>();
    private final LocalDateTime actuelLocalDateTime;
    private final NombreJourValidationVersionParametreCalendrierCalculDto nombreJourValidationVersionParametreCalendrierCalculDto;

    public GroupementVersionParametreCalendrierCalculAbsentConsumer(LocalDateTime actuelLocalDateTime, NombreJourValidationVersionParametreCalendrierCalculDto nombreJourValidationVersionParametreCalendrierCalculDto) {
        this.actuelLocalDateTime = actuelLocalDateTime;
        this.nombreJourValidationVersionParametreCalendrierCalculDto = nombreJourValidationVersionParametreCalendrierCalculDto;
    }

    @Override
    public void accept(ValidePointModeleDto validePointModeleDto3) {
        LocalDateTime finCouverturePeriode = obtenirFinCouverturePeriode(validePointModeleDto3);
        long joursValide = ChronoUnit.DAYS.between(actuelLocalDateTime, finCouverturePeriode);

        if (joursValide < nombreJourValidationVersionParametreCalendrierCalculDto.getApres()) {
            GroupementValidePointModelDto groupementValidePointModelDto = trouverGroupementValidePointModele(joursValide, finCouverturePeriode);
            groupementValidePointModelDto.getInvalidPointModeleDtos().add(validePointModeleDto3);
        }
    }

    private LocalDateTime obtenirFinCouverturePeriode(ValidePointModeleDto validePointModeleDto) {
        // la période de validité couverte commence de -7 jours à 50 jours.  Pour une durée de 57 jours
        LocalDateTime finCouverturePeriode = actuelLocalDateTime.minusDays(nombreJourValidationVersionParametreCalendrierCalculDto.getAvant());

        for (PointModelDto pointModelDto : validePointModeleDto.getPeriodePointModeleDtos()) {

            if ((pointModelDto.getDateDebEffective().isBefore(finCouverturePeriode) || pointModelDto.getDateDebEffective().isEqual(finCouverturePeriode))
                    && (pointModelDto.getDateFinEffective().isAfter(finCouverturePeriode) || pointModelDto.getDateFinEffective().isEqual(finCouverturePeriode))) {
                finCouverturePeriode = pointModelDto.getDateFinEffective();
            }
        }
        return finCouverturePeriode;
    }

    private GroupementValidePointModelDto trouverGroupementValidePointModele(long joursValide, LocalDateTime finCouvertureLocalDateTime) {
        Optional<GroupementValidePointModelDto> groupementValidePointModele = groupementInvalidePointModeles.stream()
                .filter(g -> g.getJoursRestantCouverture() == joursValide).findFirst();

        if (groupementValidePointModele.isPresent()) {
            return groupementValidePointModele.get();
        }
        // le groupement n'a pas été trouvé on en créer un
        GroupementValidePointModelDto groupementValidePointModelDto = GroupementValidePointModelDto.builder()
                .couvertureLocalDateTime(finCouvertureLocalDateTime)
                .joursRestantCouverture(joursValide)
                .build();
        groupementInvalidePointModeles.add(groupementValidePointModelDto);
        return groupementValidePointModelDto;
    }

    public List<GroupementValidePointModelDto> groupementInvalidePointModeles() {
        return groupementInvalidePointModeles;
    }

}
