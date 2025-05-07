package ca.qc.hydro.epd.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@IdClass(ActionAttributId.class)
@Table(name = "PDC1204_ACTION_ATRIBUT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ActionAttribut implements CompositeKeyEntity<ActionAttributId> {

    @Id
    @OneToOne
    @JoinColumn(name = "ID_ACT", nullable = false)
    private ActionJournal action;

    @Id
    @OneToOne
    @JoinColumn(name = "ID_ATR", nullable = false)
    private AttributJournal attribut;

    @Override
    public ActionAttributId getId() {
        return ActionAttributId.builder().action(this.action).attribut(this.attribut).build();
    }

}
