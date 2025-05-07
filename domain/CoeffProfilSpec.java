package ca.qc.hydro.epd.domain;

import java.sql.Clob;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.dto.JsonViews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(CoeffProfilSpecId.class)
@Table(name = "PDC615_COEFF_PROFIL_SPEC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CoeffProfilSpec extends AbstractAuditableEntity implements CompositeKeyEntity<CoeffProfilSpecId> {

    @JsonView(JsonViews.CoeffProfSpecIdOnlyView.class)
    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @JsonView(JsonViews.CoeffProfSpecIdOnlyView.class)
    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    private PointModele pointModele;

    @JsonView(JsonViews.CoeffProfSpecIdOnlyView.class)
    @Id
    @Column(name = "NO_PROF_SPEC", nullable = false)
    private Integer noProfil;

    @JsonView(JsonViews.CoeffProfSpecIdOnlyView.class)
    @Id
    @Column(name = "AN_PROF_SPEC", nullable = false)
    private Integer annee;

    @Column(name = "COEFF_PROF_SPEC", nullable = false)
    @Lob
    private Clob coeffProfil;

    @Column(name = "PAS_DE_TEMPS", nullable = false)
    private Integer pasDeTemps;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "NO_PROF_SPEC", referencedColumnName = "NO_PROF_SPEC", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private ProfilSpec profilSpec;

    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "coeffProfilSpec")
    private Set<AssoProfilSpec> assoProfilsSpec = new HashSet<>();

    public CoeffProfilSpec(CoeffProfilSpecId id) {
        this.pointId = id.getPointId();
        this.codeModele = id.getCodeModele();
        this.annee = id.getAnnee();
        this.noProfil = id.getNoProfil();
    }

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public CoeffProfilSpecId getId() {
        return CoeffProfilSpecId.builder().pointId(this.pointId).codeModele(this.codeModele).noProfil(this.noProfil).annee(this.annee).build();
    }
}
