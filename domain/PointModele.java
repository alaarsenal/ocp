package ca.qc.hydro.epd.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@IdClass(PointModeleId.class)
@Table(name = "PDC602_POINT_MOD")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PointModele extends AbstractEntity implements CompositeKeyEntity<PointModeleId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private Point point;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private Modele modele;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pointModele")
    private Set<PointModeleFonction> pointModeleFonction = new HashSet<>();

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public PointModeleId getId() {
        return PointModeleId.builder().pointId(this.pointId).codeModele(this.codeModele).build();
    }
}
