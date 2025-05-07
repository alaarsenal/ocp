package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

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
@Table(name = "PDC1001_PREV_ETUD")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@JsonView(JsonViews.PrevisionEtudeDetailsView.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/*
 * Cet objet contient les caractéristiques générales des lots de prévisions d'étude.
 */
public class PrevisionEtude extends AbstractEntity {

    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO_PREV_ETUD", nullable = false)
    @EqualsAndHashCode.Include
    private Long noPrevEtude;

    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "COD_PREV_ETUD", nullable = false, unique = true)
    private String code;

    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "NOM_PREV_ETUD", nullable = false, length = 20)
    private String nom;

    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "DESC_PREV_ETUD", nullable = false, length = 200)
    private String description;

    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "CII_PREV_ETUD", nullable = false)
    private String proprietaire;

    /**
     * Indique si le lot est public ou privé. 'O': Le groupement est public 'N': Le groupement est privé (et visible
     * uniquement par son propriétaire) Valeur par défaut: 'O'
     */
    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "IND_PUBLIC", nullable = false, columnDefinition = "char default 'O' not null")
    private Character indPublic;

    /**
     * Date d'enregistrement du lot de prévision d'étude
     */
    @Column(name = "D_ENR_ETUD", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnrEtude;

    /**
     * Date du 1er jour de la période d'étude
     */
    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "D_DEB_HORIZ_ETUD", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDebEtude;

    /**
     * Position par rapport au jour courant du 1er jour de la période d'étude.  Optionnel: si présent, a préséance sur
     * la date de début de période (D_DEB_HORIZ_ETUD)
     */
    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "J_REF_VS_J_COUR")
    private Integer jourRefVsJourCourant;

    /**
     * Nombre de jours de la période d'étude
     */
    @JsonView(JsonViews.PrevisionEtudeBaseView.class)
    @Column(name = "NB_JRS", nullable = false)
    private Long nbJours;

}
