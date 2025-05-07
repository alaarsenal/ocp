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
public class HistoConsRequest implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = 6260621435234482215L;

    /**
     * Date du jour prévu AAAA-MM-JJTHH:MM:SSZ (UTC)
     */
    @JsonProperty("jourConsDeb")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourHistoConsDeb;

    /**
     * Date du jour prévu AAAA-MM-JJTHH:MM:SSZ (UTC)
     */
    @JsonProperty("jourConsFin")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourHistoConsFin;

    /**
     * Code du groupement de points de prévision de la demande
     */
    @JsonProperty("codGrp")
    private String codGrp;

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
     * Opération à effectuer sur les consommations retournées (optionnel)
     */
    @JsonProperty("operCons")
    private String operCons;

    /**
     * Indicateur de conversion des jours de changement d'heures (optionnel, défaut = false)
     */
    @JsonProperty("indConvJour")
    private Character indConvJour;

    /**
     * Liste pour déterminer la source des consommations à retourner (optionnel)
     */
    @Builder.Default
    @JsonProperty("listCodSrc")
    private List<String> codSrcList = new ArrayList<>();

    /**
     * Liste pour déterminer la portée et le type des consommations à retourner (optionnel)
     */
    @Builder.Default
    @JsonProperty("listDefDonCons")
    private List<HistoConsDefDonPair> defDonPairs = new ArrayList<>();

}
