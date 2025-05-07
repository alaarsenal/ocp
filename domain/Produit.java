package ca.qc.hydro.epd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(ProduitId.class)
@Table(name = "PDC204_PRODUIT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Produit extends AbstractEntity implements CompositeKeyEntity<ProduitId> {

    @Id
    @Column(name = "COD_SRC", nullable = false)
    private String codeSource;

    @Id
    @Column(name = "COD_PROD", nullable = false)
    private String codeProduit;

    @Column(name = "NOM_PROD", nullable = false, length = 40)
    private String nomProduit;

    @Column(name = "IND_PD_ETUD", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indPdEtud;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public ProduitId getId() {
        return ProduitId.builder().codeSource(this.codeSource).codeProduit(this.codeProduit).build();
    }
}
