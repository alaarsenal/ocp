package ca.qc.hydro.epd.service.dashboardmessage;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import ca.qc.hydro.epd.dto.PointModelDto;
import ca.qc.hydro.epd.dto.ValidePointModeleDto;

public class CumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer implements Consumer<PointModelDto> {

    private final List<ValidePointModeleDto> cumulerPeriodeValidePointModeles;

    public CumulerPeriodeVersionParametreCalendrierCalculAbsentConsumer(List<ValidePointModeleDto> validePointModeles) {
        this.cumulerPeriodeValidePointModeles = validePointModeles;
    }

    @Override
    public void accept(PointModelDto pointModelDto) {
        if (pointModelDto != null
                && StringUtils.hasText(pointModelDto.getCodeModele())
                && StringUtils.hasText(pointModelDto.getCodeRefPoint())
                && pointModelDto.getDateDebEffective() != null
                && pointModelDto.getDateFinEffective() != null) {
            ValidePointModeleDto trouveValidePointModele = trouverValidePointModele(pointModelDto);

            if (trouveValidePointModele != null) {
                trouveValidePointModele.getPeriodePointModeleDtos().add(pointModelDto);
            }
        }
    }

    private ValidePointModeleDto trouverValidePointModele(PointModelDto pointModelDto) {
        Optional<ValidePointModeleDto> validePointModele = cumulerPeriodeValidePointModeles.stream()
                .filter(rm -> rm.getCodeModele().equals(pointModelDto.getCodeModele())
                        && rm.getCodeRefPoint().equals(pointModelDto.getCodeRefPoint())).findFirst();

        return validePointModele.orElse(null);
    }

    public List<ValidePointModeleDto> cumulerPeriodeValidePointModeles() {
        return cumulerPeriodeValidePointModeles;
    }

}
