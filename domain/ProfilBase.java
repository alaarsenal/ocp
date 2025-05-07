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
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(ProfilBaseId.class)
@Table(name = "PDC613_PROFIL_BASE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProfilBase extends AbstractEntity implements CompositeKeyEntity<ProfilBaseId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    private PointModele pointModele;

    @Id
    @Column(name = "NO_PROF_BASE", nullable = false)
    private Integer noProfil;

    @Column(name = "NOM_PROF_BASE", nullable = false)
    private String nom;

    @Column(name = "DESC_PROF_BASE", nullable = false)
    private String description;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profilBase")
    private Set<AssoProfilBase> assoProfilsBase = new HashSet<>();

    public ProfilBase(ProfilBaseId id) {
        this.pointId = id.getPointId();
        this.codeModele = id.getCodeModele();
        this.noProfil = id.getNoProfil();
    }

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public ProfilBaseId getId() {
        return ProfilBaseId.builder().pointId(this.pointId).codeModele(this.codeModele).noProfil(this.noProfil).build();
    }
}
