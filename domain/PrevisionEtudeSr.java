package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC1002_PREV_ETUD_SR")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/*
 * Cet objet identifie les sous points qui seront calculés par le lot de prévision d'étude.
 */
public class PrevisionEtudeSr extends AbstractEntity {

    @Id
    @Column(name = "NO_PREV_ETUD", nullable = false)
    @EqualsAndHashCode.Include
    private Long noPrevEtude;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "NO_PREV_ETUD", nullable = false, insertable = false, updatable = false)
    private PrevisionEtude prevEtude;

    @Column(name = "COD_GRP", nullable = false)
    private String codeGrp;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_GRP", referencedColumnName = "COD_GRP", nullable = false, insertable = false, updatable = false)
    private GroupePoint groupePoint;

}
