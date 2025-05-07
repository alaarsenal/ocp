package ca.qc.hydro.epd.domain;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "PDC102_GRP_POINT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class GroupePoint extends AbstractEntity {

    @Id
    @Column(name = "COD_GRP", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_GRP", nullable = false)
    private String nom;

    @Column(name = "DESC_GRP", nullable = false)
    private String description;

    @Column(name = "IND_GRP_SYST", nullable = false)
    private Character indGrpSyst;

    @Column(name = "IND_GRP_AFFICH", nullable = false)
    private Character indGrpAffich;

    // The Jackson annotations are inverted here and this is wanted!
    @JsonManagedReference
    @OneToMany(mappedBy = "groupePoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ComposanteGroupement> composantes;

    public GroupePoint(String code, String nom, String description, Character indGrpSyst, Character indGrpAffich) {
        this.code = code;
        this.nom = nom;
        this.description = description;
        this.indGrpSyst = indGrpSyst;
        this.indGrpAffich = indGrpAffich;
    }

}
