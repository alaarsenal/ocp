package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC703_TYPE_HORIZON_CALC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TypeHorizonCalc extends AbstractEntity {

    @Id
    @Column(name = "COD_TYP_HOR", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_TYP_HOR", nullable = false)
    private String nom;

    @Column(name = "DESC_TYP_HOR", nullable = false)
    private String description;

    @Column(name = "NO_RANG_CALC", nullable = false)
    private Integer noRangCalc;

}
