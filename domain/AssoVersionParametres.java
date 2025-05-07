package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import ca.qc.hydro.epd.dto.JsonViews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "PDC612_ASSO_VP",
        uniqueConstraints = @UniqueConstraint(columnNames = {"VP_ID", "D_DEB_EFFEC", "D_ENR_EFFEC"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AssoVersionParametres extends AbstractAuditableEntity {

    @JsonView(JsonViews.AssoVersionParametresIdOnlyView.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSO_VP_ID", unique = true, nullable = false, precision = 22)
    @EqualsAndHashCode.Include
    private BigDecimal assoVpId;

    @JsonView(JsonViews.AssoVersionParametresView.class)
    @Column(name = "D_ENR_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnrEffective;

    @JsonView(JsonViews.AssoVersionParametresView.class)
    @Column(name = "D_DEB_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateDebEffective;

    @JsonView(JsonViews.AssoVersionParametresView.class)
    @Column(name = "D_FIN_EFFEC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateFinEffective;

    @JsonView(JsonViews.AssoVersionParametresView.class)
    @Column(name = "NOTE_VP")
    private String noteVp;

    @JsonView(JsonViews.AssoVersionParametresView.class)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VP_ID", referencedColumnName = "VP_ID", nullable = false, updatable = false)
    @JsonManagedReference
    private VersionParametres versionParametres;

}
