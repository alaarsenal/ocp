package ca.qc.hydro.epd.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC603_FONC")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Fonction extends AbstractEntity {

    @Id
    @Column(name = "COD_FONC", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_FONC", nullable = false)
    private String nom;

    @Column(name = "TYPE_FONC", nullable = false)
    private String type;

    @Column(name = "IND_POND", nullable = false)
    private Character indPonderation;

    @Column(name = "DESC_FONC", nullable = false)
    private String description;

    @Column(name = "PAS_DE_TEMPS", nullable = false)
    private Integer pasDeTemps;

    @Column(name = "IND_EPURE", nullable = false)
    private Character indEpure;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fonction")
    private Set<PointModeleFonction> pointModeleFonctions = new HashSet<>();
}
