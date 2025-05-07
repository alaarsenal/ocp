package ca.qc.hydro.epd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface CompositeKeyEntity<ID> { //NOSONAR <code>ID</code> is a standard generic name used by all Spring repositories

    @JsonIgnore
    ID getId();

}
