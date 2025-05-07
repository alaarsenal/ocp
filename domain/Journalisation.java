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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC1201_JOURNALISATION")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Journalisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_JRNL", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "PARENT_ID_JRNL")
    private Long parentId;

    @Column(name = "DATE_DEBUT_ACT", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDebut;

    @Column(name = "DATE_FIN_ACT", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateFin;

    @Column(name = "DATE_CREAT_JRNL", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnreg;

    @Column(name = "GROUPEMENT_ACT")
    private String groupement;

    @Column(name = "POINT_ACT")
    private String point;

    @ManyToOne
    @JoinColumn(name = "ACT_JRNL")
    private ActionJournal action;

    @Column(name = "MODEL_ACT")
    private String model;

    @Column(name = "RAISON_ACT")
    private String raison;

    @Column(name = "NOM_ACT")
    private String actionNom;

    @Column(name = "SYSTEM_ACT")
    private String system;

    @Column(name = "NUM_SUIVI_ACT")
    private String numSuivi;

    @Column(name = "IMPACT_ACT")
    private String impact;

    @Column(name = "TYPE_ACT")
    private String type;

    @Column(name = "SOURCE_ACT")
    private String source;

    @Column(name = "PERFORMANCE_ACT")
    private Boolean performance;

    @Column(name = "ESTIMATION_ACT")
    private Boolean estimation;

    @Column(name = "CIP_JRNL")
    private String cip;

    @Column(name = "USAGER_PRENOM_JRNL", nullable = false)
    private String usagerPrenom;

    @Column(name = "USAGER_NOM_JRNL", nullable = false)
    private String usagerNom;

    @Column(name = "COMMENTAIRE")
    private String commentaire;

    @Column(name = "MANUAL_JRNL", nullable = false)
    private boolean manual;
}
