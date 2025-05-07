package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.dto.JsonViews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC101_POINTS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Point extends AbstractEntity {

    @Id
    @JsonView(JsonViews.PointBaseView.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Convert(converter = LongToBigDecimalConverter.class)
    @Column(name = "POINT_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @JsonView(JsonViews.PointBaseView.class)
    @Column(name = "COD_POINT", nullable = false)
    private String code;

    @JsonView(JsonViews.PointBaseView.class)
    @Column(name = "COD_REF_POINT", nullable = false)
    private String codeRef;

    @JsonView(JsonViews.PointBaseView.class)
    @Column(name = "NOM_POINT", nullable = false)
    private String nom;

    @Column(name = "DESC_POINT", nullable = false)
    private String description;

    @Column(name = "SEUIL_ECART_PREV", nullable = false)
    private BigDecimal seuilEcartPrev;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COD_TYP_POINT", nullable = false)
    private TypePoint type;

    @Convert(converter = PointGeoLocConverter.class)
    @Column(name = "GEO_LOC", nullable = false)
    private PointGeoLoc geoLoc;

    @JsonBackReference
    @OneToMany(mappedBy = "point")
    @ToString.Exclude
    private Set<ComposanteGroupement> composantes;

    @JsonBackReference
    @OneToMany(mappedBy = "point")
    @ToString.Exclude
    private Set<PointModele> pointModeles;

    public Point(Long id, String code, String codeRef, String nom, String description, BigDecimal seuilEcartPrev) {
        this.id = id;
        this.code = code;
        this.codeRef = codeRef;
        this.nom = nom;
        this.description = description;
        this.seuilEcartPrev = seuilEcartPrev;
    }

}
