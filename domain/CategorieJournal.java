package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC1202_CATEGORIE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CategorieJournal {

    @Id
    @Column(name = "ID_CAT", nullable = false)
    private Long id;

    @Column(name = "NOM_CAT", nullable = false)
    private String nom;

    @Column(name = "DESC_CAT")
    private String description;

    @Column(name = "DATE_CREAT_CAT", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "CIP_CREAT_CAT", nullable = false)
    private String cipCreation;

    @Column(name = "DATE_MAJ_CAT", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateMAJ;

    @Column(name = "CIP_MAJ_CAT")
    private String cipMAJ;

}
