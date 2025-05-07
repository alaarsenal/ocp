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
public class DonneesCons extends DonneesHorodate implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -1774736970133729339L;

    @JsonProperty("val")
    public String valeur;
}
