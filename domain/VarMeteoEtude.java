package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC912_VAR_METE_ETUD")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/**
 * Cet objet contient les associations de chaque météo d'étude avec ses données météo (température, nébulosité, etc.)
 */
public class VarMeteoEtude extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO_VAR_METE", nullable = false)
    @EqualsAndHashCode.Include
    private Long noVarMeteo;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "NO_METE_ETUD", referencedColumnName = "NO_METE_ETUD", nullable = false, updatable = false)
    @JsonManagedReference
    private MeteoEtude meteoEtude;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "TYPE_DON", referencedColumnName = "TYPE_DON", nullable = false, updatable = false)
    @JoinColumn(name = "NIV_DON", referencedColumnName = "NIV_DON", nullable = false, updatable = false)
    @JoinColumn(name = "PORTEE_DON", referencedColumnName = "PORTEE_DON", nullable = false, updatable = false)
    private DonneeMeteo donneeMeteo;

}
