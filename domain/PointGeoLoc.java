package ca.qc.hydro.epd.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointGeoLoc implements Serializable {

    private String type;
    // Pour le moment, on ne supporte que les coordonnées géographiques en format [longitude, latitude].
    // On pourrait étendre le support à d'autres formats de coordonnées géographiques.
    private Double[] coordinates;

}
