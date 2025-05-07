package ca.qc.hydro.epd.domain;

import java.sql.Clob;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@IdClass(HistoConsId.class)
@Table(name = "PDC302_HISTO_CONS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class HistoCons extends AbstractEntity implements CompositeKeyEntity<HistoConsId> {

    @Id
    @Column(name = "JOUR_CONS", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime jourCons;

    @Id
    @Column(name = "DATE_ENR", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnr;

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    private Point point;

    @Id
    @Column(name = "COD_SRC", nullable = false)
    private String codeSource;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_SRC", nullable = false, insertable = false, updatable = false)
    private SourceDonnee source;

    @Id
    @Column(name = "TYPE_CONS", nullable = false)
    private String typeCons;

    @Id
    @Column(name = "PORTEE_CONS", nullable = false)
    private String porteeCons;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "TYPE_CONS", referencedColumnName = "TYPE_CONS", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "PORTEE_CONS", referencedColumnName = "PORTEE_CONS", nullable = false, insertable = false, updatable = false)
    private TypeCons type;

    @Column(name = "DON_CONS", nullable = false)
    @Lob
    private Clob donneesCons;

    @Column(name = "NOTE", length = 200)
    private String note;

    public HistoCons(HistoConsId id) {
        this.jourCons = id.getJourCons();
        this.pointId = id.getPointId();
        this.codeSource = id.getCodeSource();
        this.typeCons = id.getTypeCons();
        this.porteeCons = id.getPorteeCons();
        this.dateEnr = id.getDateEnr();
    }

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public HistoConsId getId() {
        return HistoConsId.builder().jourCons(this.jourCons).dateEnr(this.dateEnr).pointId(this.pointId).codeSource(this.codeSource).typeCons(this.typeCons)
                .porteeCons(this.porteeCons).build();
    }

}
