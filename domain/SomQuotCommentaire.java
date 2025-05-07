package ca.qc.hydro.epd.domain;

import java.sql.Clob;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.UtcLocalDateTimeJsonSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(
        name = "PDC811_SOM_QUOT",
        uniqueConstraints = @UniqueConstraint(columnNames = {"DATE_HRE_CALC", "POINT_ID", "COD_TYP_PREV", "CODE_ETIQ", "DATE_ENR"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonIgnoreProperties({"codeTypPrevision", "etiquettePrev", "point"})
public class SomQuotCommentaire extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SOM_QUOT_ID", unique = true, nullable = false, precision = 22)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "POINT_ID", nullable = false)
    private String pointId;

    @Column(name = "COD_TYP_PREV", nullable = false)
    private String typePrevision;

    @Column(name = "CODE_ETIQ", nullable = false)
    private String etiquette;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "COD_TYP_PREV", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private TypePrevision codeTypPrevision;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "CODE_ETIQ", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private EtiquettePrev etiquettePrev;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "POINT_ID", referencedColumnName = "POINT_ID", nullable = false, insertable = false, updatable = false)
    @JsonManagedReference
    private Point point;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_HRE_CALC", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime datePrevision;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_ENR", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateEnr;

    @Column(name = "COMMENTAIRE", nullable = false)
    private Clob valeur;

    @Column(name = "USAGER_MAJ", nullable = false)
    private String usagerMaj;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_MAJ", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateMaj;

}


