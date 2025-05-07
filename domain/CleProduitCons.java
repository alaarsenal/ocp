package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-04
 */
@Entity
@IdClass(CleProduitConsId.class)
@Table(name = "PDC207_CLE_PRODUIT_CONS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CleProduitCons extends AbstractEntity implements CompositeKeyEntity<CleProduitConsId> {

    @Id
    @Column(name = "POINT_ID", nullable = false)
    private Long pointId;

    @Id
    @Column(name = "COD_SRC", nullable = false)
    private String codeSourceDonnee;

    @Id
    @Column(name = "COD_PROD", nullable = false)
    private String codeProduit;

    @Id
    @Column(name = "NO_CLE", nullable = false)
    private String noCle;

    @Id
    @Column(name = "DATE_ENR", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnr;

    @Column(name = "CLE_PRD_CONS", nullable = false)
    private String cle;

    @Column(name = "IND_CHARGEMENT", nullable = false)
    private Character indChargement;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    private Point point;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_SRC", referencedColumnName = "COD_SRC", nullable = false, insertable = false, updatable = false)
    @JoinColumn(name = "COD_PROD", referencedColumnName = "COD_PROD", nullable = false, insertable = false, updatable = false)
    private Produit produit;

    @Override
    @EqualsAndHashCode.Include
    public CleProduitConsId getId() {
        return CleProduitConsId.builder()
                .pointId(pointId)
                .codeProduit(codeProduit)
                .codeSourceDonnee(codeSourceDonnee)
                .noCle(noCle)
                .dateEnr(dateEnr)
                .build();
    }

    public void setId(CleProduitConsId id) {
        pointId = id.getPointId();
        codeProduit = id.getCodeProduit();
        codeSourceDonnee = id.getCodeSourceDonnee();
        noCle = id.getNoCle();
        dateEnr = id.getDateEnr();
    }
}
