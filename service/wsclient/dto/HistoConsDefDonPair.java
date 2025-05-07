package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;

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
@Builder
public class HistoConsDefDonPair implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -5012777179306549225L;

    /**
     * Code du type des données des clients industriels
     */
    @JsonProperty("typeCons")
    private String typeCons;

    /**
     * Code de la portée des données des clients industriels
     */
    @JsonProperty("porteeCons")
    private String porteeCons;

}
