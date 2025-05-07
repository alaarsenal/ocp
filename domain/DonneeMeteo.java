package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(DonneeMeteoId.class)
@Table(name = "PDC202_DON_METE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class DonneeMeteo extends AbstractEntity implements CompositeKeyEntity<DonneeMeteoId> {

    @Id
    @Column(name = "TYPE_DON", nullable = false)
    private String type;

    @Id
    @Column(name = "NIV_DON", nullable = false)
    private Integer niveau;

    @Id
    @Column(name = "PORTEE_DON", nullable = false)
    private String portee;

    @Column(name = "UNITE_DON", nullable = false, length = 20)
    private String unite;

    @Column(name = "DESC_DONNEE", nullable = false, length = 200)
    private String description;

    @Column(name = "BORN_MIN")
    private BigDecimal borneMin;

    @Column(name = "BORN_MAX")
    private BigDecimal borneMax;

    @Column(name = "MSG", length = 200)
    private String message;

    @Column(name = "IND_PD_ETUD", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indPdEtude;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public DonneeMeteoId getId() {
        return DonneeMeteoId.builder().type(this.type).niveau(this.niveau).portee(this.portee).build();
    }
}
