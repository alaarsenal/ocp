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
@IdClass(TypeConsId.class)
@Table(name = "PDC301_TYPE_CONS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TypeCons extends AbstractEntity implements CompositeKeyEntity<TypeConsId> {

    @Id
    @Column(name = "TYPE_CONS", nullable = false)
    private String typeCons; //NOSONAR

    @Id
    @Column(name = "PORTEE_CONS", nullable = false)
    private String porteeCons;

    @Column(name = "IND_SOM_AJUS", nullable = false, columnDefinition = "char default 'N' not null")
    private Character indSomAjus;

    @Column(name = "DESC_TYP_CONS", length = 200)
    private String description;

    /**
     * @see CompositeKeyEntity#getId()
     */
    @Override
    @EqualsAndHashCode.Include
    public TypeConsId getId() {
        return TypeConsId.builder().typeCons(this.typeCons).porteeCons(this.porteeCons).build();
    }
}
