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
@IdClass(TypeDonIndId.class)
@Table(name = "PDC401_TYPE_DON_IND")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TypeDonInd extends AbstractEntity implements CompositeKeyEntity<TypeDonIndId> {

    @Id
    @Column(name = "TYPE_DON_IND", nullable = false)
    private String typeDonInd; //NOSONAR

    @Id
    @Column(name = "PORTEE_DON_IND", nullable = false)
    private String porteeDonInd;

    @Column(name = "IND_SOM_UNIF", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indSomUnif;

    @Column(name = "DESC_TYP_DON_IND", length = 200)
    private String description;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public TypeDonIndId getId() {
        return TypeDonIndId.builder().typeDonInd(this.typeDonInd).porteeDonInd(this.porteeDonInd).build();
    }
}
