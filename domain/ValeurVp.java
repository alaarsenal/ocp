package ca.qc.hydro.epd.domain;

import java.sql.Clob;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "PDC619_VALEUR_VP")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ValeurVp extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Convert(converter = LongToBigDecimalConverter.class)
    @Column(name = "VAL_VP_ID", nullable = false)
    @EqualsAndHashCode.Include
    private long valVpId;

    @Column(name = "COD_TYP_DONNEE", nullable = false)
    private String codTypeDonnee;

    @Column(name = "IND_A_SUPPRIMER", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indASupprimer;

    @Column(name = "DONNEE_PARAM", nullable = false)
    @Lob
    @ToString.Exclude
    private Clob donneesParametres;


    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "valeurVp", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<VersionParametres> versionParametres = new HashSet<>();

    public void addVersionParametres(VersionParametres vp) {
        if (this.versionParametres == null) {
            this.versionParametres = new HashSet<>();
        }
        this.versionParametres.add(vp);
        vp.setValeurVp(this);
    }

}
