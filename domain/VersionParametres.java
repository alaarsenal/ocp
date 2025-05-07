package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.dto.JsonViews;
import ca.qc.hydro.epd.utils.Constantes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "PDC611_VERSION_PARAM",
        uniqueConstraints = @UniqueConstraint(columnNames = {"POINT_ID", "COD_MODELE", "AN_VERS_PARAM", "COD_SAISON", "NO_VERS_PARAM", "DATE_ENR_VP"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VersionParametres extends AbstractAuditableEntity {

    @Id
    @JsonView({JsonViews.VersionParametresIdOnlyView.class, JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Convert(converter = LongToBigDecimalConverter.class)
    @Column(name = "VP_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long vpId;

    @JsonView({JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @JsonView({JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    private PointModele pointModele;

    @JsonView({JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @Column(name = "AN_VERS_PARAM", nullable = false)
    private Integer annee;

    @JsonView({JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @Column(name = "COD_SAISON", nullable = false)
    private Character saison;

    @JsonView({JsonViews.VersionParametresUniqueIdView.class, JsonViews.AssoVersionParametresView.class})
    @Column(name = "NO_VERS_PARAM", nullable = false)
    private Integer noVersion;

    @JsonView(JsonViews.VersionParametresUniqueIdView.class)
    @Column(name = "DATE_ENR_VP", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnregistrement;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VAL_VP_ID", nullable = false)
    private ValeurVp valeurVp;

    @Column(name = "IND_ARCHIVEE", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indArchivee;

    @Column(name = "DESC_VERS_PARAM", nullable = false)
    private String description;

    @Column(name = "VERS_ORIG", nullable = false)
    private String versionOrig;

    @Column(name = "PAS_DE_TEMPS", nullable = false)
    private Integer pasDeTemps;

    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "versionParametres", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<AssoVersionParametres> assoVersionParametres = new HashSet<>();

    public VersionParametres(VersionParametres vpUniqueId) {
        this.vpId = vpUniqueId.getVpId();
        this.pointId = vpUniqueId.getPointId();
        this.codeModele = vpUniqueId.getCodeModele();
        this.annee = vpUniqueId.getAnnee();
        this.saison = vpUniqueId.getSaison();
        this.noVersion = vpUniqueId.getNoVersion();
        this.dateEnregistrement = vpUniqueId.getDateEnregistrement();
    }

    public void addAssoVersionParametres(AssoVersionParametres assoVp) {
        if (this.assoVersionParametres == null) {
            this.assoVersionParametres = new HashSet<>();
        }
        this.assoVersionParametres.add(assoVp);
        assoVp.setVersionParametres(this);
    }

    public void setVersionOrig(VersionParametres sourceVp) {
        StringBuilder str = new StringBuilder();
        String dateEnrFormatted = DateTimeFormatter.ofPattern(Constantes.DATETIME_FORMAT).format(this.getDateEnregistrement()) + " " + Constantes.TIMEZONE_UTC;
        str.append(sourceVp.getPointModele().getPoint().getCodeRef()).append(" ").append(sourceVp.getCodeModele()).append(" ").append(sourceVp.getAnnee()).append(sourceVp.getSaison())
                .append(sourceVp.getNoVersion()).append(" ").append(dateEnrFormatted);
        this.versionOrig = str.toString();
    }
}
