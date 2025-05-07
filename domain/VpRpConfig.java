package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
@Table(
        name = "PDC1007_VP_RP",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"NO_PREV_ETUD", "POINT_ID", "COD_MODELE", "AN_VERS_PARAM", "COD_SAISON", "NO_VERS_PARAM", "DATE_ENR_VP", "NO_METE_ETUD", "D_REF_INTR_NON_METE"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/*
 * Cet objet identifie les configurations point principal du lot de prévision d'étude.
 */
public class VpRpConfig extends AbstractEntity {

    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO_RP_CONFIG", nullable = false)
    @EqualsAndHashCode.Include
    private Long noRpConfig;

    @Column(name = "COD_RP_CONFIG", nullable = false, unique = true)
    private String code;

    @Column(name = "NOM_RP_CONFIG", nullable = false, length = 40)
    private String nom;

    @Column(name = "DESC_RP_CONFIG", nullable = false, length = 200)
    private String description;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "NO_PREV_ETUD", nullable = false, updatable = false)
    @JsonBackReference
    private PrevisionEtudeRp prevEtudeRp;

    @ToString.Exclude
    @ManyToOne
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, updatable = false)
    @JoinColumn(name = "AN_VERS_PARAM", referencedColumnName = "AN_VERS_PARAM", nullable = false, updatable = false)
    @JoinColumn(name = "COD_SAISON", referencedColumnName = "COD_SAISON", nullable = false, updatable = false)
    @JoinColumn(name = "NO_VERS_PARAM", referencedColumnName = "NO_VERS_PARAM", nullable = false, updatable = false)
    @JoinColumn(name = "DATE_ENR_VP", referencedColumnName = "DATE_ENR_VP", nullable = false, updatable = false)
    private VersionParametres versionParametres;

    @ToString.Exclude
    @ManyToOne
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @JoinColumn(name = "NO_METE_ETUD", nullable = false, updatable = false)
    private MeteoEtude meteoEtude;

    /**
     * Date de référence du 1er jour de l'intrant non météo (consommations, variations industrielles)
     */
    @Column(name = "D_REF_INTR_NON_METE", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateRefIntrNonMeteo;

    /**
     * Position par rapport au jour courant du 1er jour de l'intrant non météo (consommations, variations
     * industrielles). Optionnel: si présent, a préséance sur la date de référence (D_REF_INTR_NON_METE)
     */
    @Column(name = "J_REF_VS_J_COUR")
    private Integer jourRefVsJourCourant;

    /**
     * Indique si les profils de base du calendrier doivent être appliqués ou non. 'O': appliquer les profils de base
     * 'N': ne pas appliquer les profils de base. Valeur par défaut: 'N'
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "IND_P_BASE_CALEND", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indProfilBaseCalendrier;

    /**
     * Numéro du profil de base. Si fourni a préséance sur IND_P_BASE_CALEND = 'O'.
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "NO_P_BASE")
    private Integer noProfilBase;

    /**
     * Indique si les profils spéciaux du calendrier doivent être appliqués ou non. 'O': appliquer les profils spéciaux
     * 'N': ne pas appliquer les profils spéciaux. Valeur par défaut: 'N'
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "IND_P_SPEC_CALEND", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indProfilSpecCalendrier;

    /**
     * Année du profil spécial. Si fourni avec NO_P_SPEC, a préséance sur IND_P_SPEC_CALEND = 'O'.
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "AN_P_SPEC")
    private Integer anProfilSpec;

    /**
     * Numéro du profil spécial. Si fourni avec AN_P_SPEC, a préséance sur IND_P_SPEC_CALEND = 'O'.
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "NO_P_SPEC")
    private Integer noProfilSpec;

    /**
     * Indique si les corrections du calendrier doivent être appliquées ou non. 'O': appliquer les corrections 'N': ne
     * pas appliquer les corrections Valeur par défaut: 'N'
     */
    @JsonView(JsonViews.PrevisionEtudeDetailsView.class)
    @Column(name = "IND_COR_CALEND", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indCorrCalendrier;

}
