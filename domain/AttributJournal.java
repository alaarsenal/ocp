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
@Table(name = "PDC1205_ATRIBUT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AttributJournal {

    @Id
    @Column(name = "ID_ATR", nullable = false)
    private Long id;

    @Column(name = "NOM_ATR", nullable = false)
    private String nom;

    @Column(name = "TYPE_ATR", nullable = false)
    private String type;

    @Column(name = "DATE_CREAT_ATR", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "CIP_CREAT_ATR", nullable = false)
    private String cipCreation;

    @Column(name = "DATE_MAJ_ATR", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateMAJ;

    @Column(name = "CIP_MAJ_ATR")
    private String cipMAJ;

}
