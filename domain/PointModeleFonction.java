package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(PointModeleFonctionId.class)
@Table(name = "PDC605_POINT_MOD_FONC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PointModeleFonction extends AbstractEntity implements CompositeKeyEntity<PointModeleFonctionId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @Column(name = "TYP_CALC", nullable = false)
    private String typeCalcul;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    private PointModele pointModele;

    @Id
    @Column(name = "COD_FONC", nullable = false)
    private String codeFonction;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_FONC", nullable = false, insertable = false, updatable = false)
    private Fonction fonction;

    @Column(name = "IND_ACTIVE", nullable = false)
    private Character indActive;

    @Column(name = "IND_RETOUR_LPS", nullable = false)
    private Character indRetourLps;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public PointModeleFonctionId getId() {
        return PointModeleFonctionId.builder().pointId(this.pointId).codeModele(this.codeModele).codeFonction(this.codeFonction).build();
    }
}
