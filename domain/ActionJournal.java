package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "PDC1203_ACTION")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ActionJournal {

    @Id
    @Column(name = "ID_ACT", nullable = false)
    private Long id;

    @Column(name = "COD_ACT", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EActionJournalCode code;

    @Column(name = "NOM_ACT", nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "CAT_ACT", nullable = false)
    private CategorieJournal categorie;

    @Column(name = "DESC_ACT")
    private String description;

    @Column(name = "DATE_CREAT_ACT", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "CIP_CREAT_ACT", nullable = false)
    private String cipCreation;

    @Column(name = "DATE_MAJ_ACT", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateMAJ;

    @Column(name = "CIP_MAJ_ACT")
    private String cipMAJ;

    public enum EActionJournalCode {
        BILAN,
        CORRECTION,
        PONDERATION,
        NOUVELLE_VP,
        FERMETURE_JOURNEE,
        PANNE_INFORMATIQUE,
        PROFILS,
        EXCLURE_DONNEES
    }

}
