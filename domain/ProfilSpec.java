package ca.qc.hydro.epd.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC617_PROFIL_SPEC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProfilSpec extends AbstractEntity {

    @Id
    @Column(name = "NO_PROF_SPEC", nullable = false)
    @EqualsAndHashCode.Include
    private Integer noProfil;

    @Column(name = "NOM_PROF_SPEC", nullable = false)
    private String nom;

    @Column(name = "DESC_PROF_SPEC", nullable = false)
    private String description;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profilSpec")
    private Set<CoeffProfilSpec> coeffProfilsSpec = new HashSet<>();
}
