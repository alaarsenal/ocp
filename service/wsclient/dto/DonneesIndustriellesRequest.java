package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Builder
public class DonneesIndustriellesRequest implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -3362659126072919225L;

    /**
     * Date/heure de debut d'extraction AAA-MM-JJTHH:MM:SSZ (UTC)
     */
    @JsonProperty("jourDonIndDeb")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourDonIndDeb;

    /**
     * Date/heure de fin d'extraction AAA-MM-JJTHH:MM:SSZ (UTC)
     */
    @JsonProperty("jourDonIndFin")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourDonIndFin;

    /**
     * Code du groupement de points de prévision de la demande
     */
    @JsonProperty("codGrp")
    private String codeGrp;

    /**
     * Code de point (requis si codGrp est absent)<br> Si codPoint et codGrp sont spécifiés, codGrp est ignoré.
     */
    @JsonProperty("codPoint")
    private String codPoint;

    /**
     * Date du jour prévu AAAA-MM-JJTHH:MM:SSZ (UTC)
     */
    @JsonProperty("dateHreRef")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime dateHreRef;

    /**
     * Indicateur de conversion des jours de changement d'heures (optionnel, défaut = false)
     */
    @JsonProperty("indConvJour")
    private Character indConvJour;

    /**
     * Liste de mode des variations des consommations des clients industriels
     */
    @Builder.Default
    @JsonProperty("listModDonInd")
    private List<String> modDonIndList = new ArrayList<>();

    /**
     * Liste de définitions de la données industrielle
     */
    @Builder.Default
    @JsonProperty("listDefDonInd")
    private List<DonneesIndustriellesDefDonPair> defDonPairs = new ArrayList<>();

}
