package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import ca.qc.hydro.epd.batch.utils.JsonUtils;

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
public class DonneesIndustriellesResult implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = 6734905079358153438L;

    /** ID point de la prévision de la demande */
    private Long idPoint;

    /** code point de la prévision de la demande */
    private String codPoint;

    /** codeRef point de la prévision de la demande */
    private String codRefPoint;

    /**
     * Mode des variations des consommations des clients industriels
     */
    private String modDonInd;

    /**
     * Code du type des données des clients industriels
     */
    private String typeDonInd;

    /**
     * Code de la portée des données des clients industriels
     */
    private String porteeDonInd;

    /**
     * Date du jour de l'occurence de la donnée (date UTC)
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourDonInd;

    /**
     * Date d'enregistrement (et d'effet) de la courbe quotidienne des données industrielles dans le système
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime dateEnr;

    /**
     * Suite des uplets (horodate, qualite, valSansPerteMW, valAvecPerteMW)
     */
    @JsonProperty("donInd")
    private String donneesIndStr;

    public DonneesIndWrapper getDonneesInd() throws IOException {
        return JsonUtils.convert(this.donneesIndStr, DonneesIndWrapper.class);
    }
}
