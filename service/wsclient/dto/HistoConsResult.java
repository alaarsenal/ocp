package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class HistoConsResult implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = 5620574433972377785L;

    /** Identification du point */
    private Long idPoint;

    /** Représentation externe de idPoint */
    private String codPoint;

    /**
     * Le code point de la prévision de la demande
     */
    private String codRefPoint;

    /**
     * Le code de la source de données
     */
    private String codeSrc;

    /**
     * Le code du type de consommation
     */
    private String typeCons;

    /**
     * Le code de portee de consommation
     */
    private String porteeCons;

    /**
     * La description de la portee de consommation
     */
    private String descTypeCons;

    /**
     * Date du jour de l'occurence de la consommation (date UTC)
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourCons;

    /**
     * Date d'enregistrement de l'occurence de la consommation dans la BD (date UTC)
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime dateEnr;

    /**
     * Suite des triplets (horodate)
     */
    @JsonProperty("donCons")
    private DonneesConsWrapper donneesCons;

    /**
     * Note sur l'occurence de la consommation
     */
    private String note;
}
