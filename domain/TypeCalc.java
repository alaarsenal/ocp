package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@IdClass(TypeCalcId.class)
@Table(name = "PDC702_TYPE_CALCUL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TypeCalc extends AbstractEntity implements CompositeKeyEntity<TypeCalcId> {

    @Id
    @Column(name = "COD_TYP_PREV", nullable = false)
    private String codeTypePrev;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_TYP_PREV", nullable = false, insertable = false, updatable = false)
    private TypePrevision typePrev;

    @Id
    @Column(name = "COD_TYP_HOR", nullable = false)
    private String codeTypeHor;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_TYP_HOR", nullable = false, insertable = false, updatable = false)
    private TypeHorizonCalc typeHorCalc;

    @Column(name = "COD_GRP_POINT", nullable = false)
    private String codeGrp;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_GRP_POINT", referencedColumnName = "COD_GRP", nullable = false, insertable = false, updatable = false)
    private GroupePoint groupePoint;

    @Column(name = "NB_JRS_AV", nullable = false)
    private Integer nbJoursAvant;

    @Column(name = "NB_JRS_AP", nullable = false)
    private Integer nbJoursApres;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public TypeCalcId getId() {
        return TypeCalcId.builder().codeTypePrev(this.codeTypePrev).codeTypeHor(this.codeTypeHor).build();
    }
}
