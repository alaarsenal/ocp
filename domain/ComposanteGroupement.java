package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(ComposanteGroupementId.class)
@Table(name = "PDC103_COMPOSANTE_GRP")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ComposanteGroupement extends AbstractEntity implements CompositeKeyEntity<ComposanteGroupementId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_GRP", nullable = false)
    private String codeGroupe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    @ToString.Exclude
    private Point point;

    @ManyToOne
    @JoinColumn(name = "COD_GRP", nullable = false, insertable = false, updatable = false)
    // The Jackson annotations are inverted here and this is wanted!
    @JsonBackReference
    @ToString.Exclude
    private GroupePoint groupePoint;

    @Column(name = "NO_ORDRE_COMPOS")
    private BigDecimal noOrdreComposante;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public ComposanteGroupementId getId() {
        return ComposanteGroupementId.builder().pointId(this.pointId).codeGroupe(this.codeGroupe).build();
    }
}
