package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC105_BORNE_PUIS_POINT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@JsonIgnoreProperties({"point", "borne"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class BornePuis extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BORNE_PUIS_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Column(name = "COD_BORNE", nullable = false)
    private String codeBorne;

    @Column(name = "TYPE_BORNE", nullable = false)
    private String typeBorne;

    @Column(name = "NO_MOIS", nullable = false)
    private Integer noMois;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private Point point;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_BORNE", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private Borne borne;

    @Column(name = "VALEUR", nullable = false)
    private BigDecimal valeur;

}
