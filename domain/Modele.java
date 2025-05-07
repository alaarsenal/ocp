package ca.qc.hydro.epd.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "PDC601_MODELE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Modele extends AbstractEntity {

    @Id
    @Column(name = "COD_MODELE", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_MODELE", nullable = false)
    private String nom;

    @Column(name = "DESC_MODELE", nullable = false)
    private String description;

    @Column(name = "MODELE_MIN", nullable = false)
    private String modeleMin;

    @Column(name = "IND_CFG", nullable = false)
    private Character indConfig;

    @Column(name = "IND_VP", nullable = false)
    @Convert(converter = BooleanToCharConverter.class)
    private boolean indicateurVersionParametre;

    @JsonBackReference
    @OneToMany(mappedBy = "modele")
    private Set<PointModele> pointModeles;

    @JsonIgnore
    public boolean isConfigurable() {
        return this.indConfig == 'O';
    }

}
