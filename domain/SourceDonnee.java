package ca.qc.hydro.epd.domain;

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
@Table(name = "PDC203_SOURCE_DONNEE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class SourceDonnee extends AbstractEntity {

    @Id
    @Column(name = "COD_SRC", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_SRC", nullable = false)
    private String nom;
}
