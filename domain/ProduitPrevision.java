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
@Table(name = "PDC808_PRODUIT_PREVISION")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProduitPrevision extends AbstractEntity {

    @Id
    @Column(name = "COD_PRODUIT_PREV", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_PRODUIT_PREV", nullable = false)
    private String nom;

    @Column(name = "DESC_PRODUIT_PREV", nullable = false)
    private String description;

    @Column(name = "IND_PROD_PRINC", nullable = false)
    private Character indProdPrinc;

}
