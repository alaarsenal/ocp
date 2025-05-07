package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class DonneesIndWrapper implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -1923353940460503409L;

    @JsonProperty("valeurs")
    private List<DonneesInd> valeurs;
}
