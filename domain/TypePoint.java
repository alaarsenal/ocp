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
@Table(name = "PDC106_TYP_POINT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TypePoint extends AbstractEntity {

    @Id
    @Column(name = "COD_TYP_POINT", nullable = false, length = 10)
    private String code;

    @Column(name = "RANG_TYP_POINT", nullable = false, precision = 2)
    private int rang;

    @Column(name = "NOM_TYP_POINT", nullable = false, length = 40)
    private String nom;

    @Column(name = "DESC_TYP_POINT", nullable = false, length = 200)
    private String description;

}

