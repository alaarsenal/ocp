package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "PDC911_METE_ETUD")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/**
 * Cet objet contient les caractéristiques générales des lots de la météo d'étude.
 */
public class MeteoEtude extends AbstractEntity {

    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO_METE_ETUD", nullable = false)
    @EqualsAndHashCode.Include
    private Long noMeteoEtude;

    @Column(name = "COD_METE_ETUD", nullable = false, unique = true)
    private String code;

    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "NOM_METE_ETUD", nullable = false, length = 20)
    private String nom;

    @Column(name = "DESC_METE_ETUD", nullable = false, length = 200)
    private String description;

    @Column(name = "CII_METE_ETUD", nullable = false)
    private String proprietaire;

    /**
     * Indique si la météo d'étude est publique ou privée. 'O': Publique 'N': Privée (et visible uniquement par son
     * propriétaire) Valeur par défaut: 'O'
     */
    @Column(name = "IND_PUBLIC", nullable = false, columnDefinition = "char default 'O' not null")
    private Character indPublic;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_SRC", referencedColumnName = "COD_SRC", nullable = false, updatable = false)
    @JoinColumn(name = "COD_PROD", referencedColumnName = "COD_PROD", nullable = false, updatable = false)
    private Produit produit;

    /**
     * Date d'enregistrement de la météo d'étude
     */
    @Column(name = "D_ENR_METE_ETUD", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnrEtude;

    /**
     * Date de référence du 1er jour de l'intrant météo (observations) ou de la plus récente émission de cet intrant
     * (prévisions)
     */
    @Column(name = "D_REF_INTR_METE", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateRefIntrMeteo;

    /**
     * Position par rapport au jour courant du 1er jour de l'intrant météo (observations) ou de la plus récente émission
     * de cet intrant (prévisions). Optionnel: si présent, a préséance sur la date de référence (D_REF_INTR_METE)
     */
    @Column(name = "J_REF_VS_J_COUR")
    private Integer jourRefVsJourCourant;

    /**
     * Optionnel: Étiquette AM, PM, NU si le produit correspond à la prévision météo
     */
    @Column(name = "ETIQ_INTR_METE")
    private String etiqIntrMeteo;

    /**
     * Optionnel: numéro du 1er jour de l'intrant météo dans la période retournée (cas d'un horizon de données prévues)
     */
    @Column(name = "NO_J_DEB")
    private Integer noJourDeb;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "meteoEtude", cascade = {CascadeType.ALL})
    private Set<VarMeteoEtude> variations = new HashSet<>();

    public void addVariations(VarMeteoEtude varMeteoEtude) {
        if (this.variations == null) {
            this.variations = new HashSet<>();
        }
        varMeteoEtude.setMeteoEtude(this);
        this.variations.add(varMeteoEtude);
    }
}
