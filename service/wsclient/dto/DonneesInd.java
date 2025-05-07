package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
@JsonPropertyOrder({"horodate", "valeur"})
public class DonneesInd extends DonneesHorodate implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -6915665927033738722L;

    @JsonProperty("qualite")
    public String qualite;

    @JsonProperty("valSansPerteMW")
    public String valSansPerteMW;

    @JsonProperty("valAvecPerteMW")
    public String valAvecPerteMW;
}
