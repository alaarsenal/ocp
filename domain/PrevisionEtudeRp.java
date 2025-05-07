package ca.qc.hydro.epd.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC1006_PREV_ETUD_RP")
@PrimaryKeyJoinColumn(name = "NO_PREV_ETUD")
@NamedEntityGraph(
        name = "PrevisionEtudeRp.vpRpConfigs",
        attributeNodes = @NamedAttributeNode("vpRpConfigs")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
/*
 * Cet objet identifie le point principal qui sera calculé par le lot de prévision d'étude.
 */
public class PrevisionEtudeRp extends PrevisionEtude {

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, updatable = false)
    private Point point;

    @ToString.Exclude
    @JsonManagedReference
    @JsonProperty("vpConfigs")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "prevEtudeRp", cascade = {CascadeType.ALL})
    private Set<VpRpConfig> vpRpConfigs = new HashSet<>();

    public void addVpRpConfig(VpRpConfig vpRpConfig) {
        if (this.vpRpConfigs == null) {
            this.vpRpConfigs = new HashSet<>();
        }
        vpRpConfig.setPrevEtudeRp(this);
        this.vpRpConfigs.add(vpRpConfig);
    }

    //en attendant la refonte de la table PDC1007_VP_RP
    public boolean contains(VersionParametres vpId) {
        for (VpRpConfig vpRpConfig : this.vpRpConfigs) {
            if (vpRpConfig.getVersionParametres().getPointId().equals(vpId.getPointId())
                    && vpRpConfig.getVersionParametres().getCodeModele().equals(vpId.getCodeModele())
                    && vpRpConfig.getVersionParametres().getAnnee().equals(vpId.getAnnee())
                    && vpRpConfig.getVersionParametres().getSaison().equals(vpId.getSaison())
                    && vpRpConfig.getVersionParametres().getNoVersion().equals(vpId.getNoVersion())
                    && vpRpConfig.getVersionParametres().getDateEnregistrement().equals(vpId.getDateEnregistrement())
            ) {
                return true;
            }
        }
        return false;
    }
}
