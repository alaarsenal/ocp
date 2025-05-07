package ca.qc.hydro.epd.domain;

import java.sql.Clob;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.dto.JsonViews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(DonneesModeleId.class)
@Table(name = "PDC621_DON_MOD")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class DonneesModele extends AbstractAuditableEntity implements CompositeKeyEntity<DonneesModeleId> {

    @JsonView(JsonViews.DonneesModeleIdOnlyView.class)
    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;
    @JsonView(JsonViews.DonneesModeleIdOnlyView.class)
    @Id
    @Column(name = "COD_MODELE", nullable = false)
    private String codeModele;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_MODELE", referencedColumnName = "COD_MODELE", nullable = false, insertable = false, updatable = false)
    private PointModele pointModele;
    @JsonView(JsonViews.DonneesModeleIdOnlyView.class)
    @Id
    @Column(name = "TYP_DON_MOD", nullable = false)
    private String typeDonnee;
    @JsonView(JsonViews.DonneesModeleIdOnlyView.class)
    @Id
    @Column(name = "D_ENR_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnrEffective;
    @JsonView(JsonViews.DonneesModeleIdOnlyView.class)
    @Id
    @Column(name = "D_DEB_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDebEffective;
    @JsonView(JsonViews.DonneesModeleHistoryView.class)
    @Column(name = "D_FIN_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateFinEffective;
    @Column(name = "DON_MOD_JOURNEE", nullable = false)
    @Lob
    private Clob donneesModJournee;
    @Column(name = "NOTE_DON_MOD", length = 200)
    private String note;
    @Column(name = "PAS_DE_TEMPS", nullable = false)
    private Integer pasDeTemps;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public DonneesModeleId getId() {
        return DonneesModeleId.builder().pointId(this.pointId).codeModele(this.codeModele).typeDonnee(this.typeDonnee)
                .dateEnrEffective(this.dateEnrEffective).dateDebEffective(this.dateDebEffective).build();
    }

    @RequiredArgsConstructor
    @Getter
    public enum TypeDonnee {
        COR("COR"), POND("POND");

        @JsonValue
        private final String code;
    }
}
