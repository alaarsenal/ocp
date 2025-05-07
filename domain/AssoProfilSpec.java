package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(AssoProfilSpecId.class)
@Table(name = "PDC616_ASSO_PROF_SPEC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AssoProfilSpec extends AbstractAuditableEntity implements CompositeKeyEntity<AssoProfilSpecId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @Id
    @Column(name = "NO_PROF_SPEC", nullable = false)
    private Integer noProfil;

    @Id
    @Column(name = "AN_PROF_SPEC", nullable = false)
    private Integer annee;

    @Id
    @Column(name = "D_ENR_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnrEffective;

    @Id
    @Column(name = "D_DEB_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDebEffective;

    @Column(name = "D_FIN_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateFinEffective;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "NO_PROF_SPEC", referencedColumnName = "NO_PROF_SPEC", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "AN_PROF_SPEC", referencedColumnName = "AN_PROF_SPEC", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private CoeffProfilSpec coeffProfilSpec;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public AssoProfilSpecId getId() {
        return AssoProfilSpecId.builder()
                .pointId(this.pointId)
                .codeModele(this.codeModele)
                .noProfil(this.noProfil)
                .annee(this.annee)
                .dateEnrEffective(this.dateEnrEffective)
                .dateDebEffective(this.dateDebEffective)
                .build();
    }

}
