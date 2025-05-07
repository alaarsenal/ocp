package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.math.BigDecimal;
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
public class PrevisionPlusRecentCalcResult implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -7154264713272633918L;

    /**
     * Code du produit de prévision de la demande
     */
    private String codProduitPrev;

    /** Id du point */
    private long idPoint;

    /** Code de référence du point */
    private String codRefPoint;

    /** Code du point */
    private String codPoint;

    /**
     * Code du modèle de calcul de prévision
     */
    private String codModele;

    /**
     * Code de la fonction de calcul de prévision
     */
    private String codFonc;

    /**
     * Code du type de la prévision
     */
    private String codTypPrev;

    /**
     * Code étiquette de la prévision
     */
    private String codeEtiq;

    /**
     * Code du statut de la prévision
     */
    private String codStatutPrev;

    /**
     * Code du fournisseur météo
     */
    private String fournPrevMete;

    /**
     * Date du jour prévu
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime jourPrevu;

    /**
     * Date heure de l'exécution du calcul
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime dateHreCalc;

    /**
     * Projection horaire minimum
     */
    private BigDecimal projectHorMn;

    /**
     * Projection horaire maximum
     */
    private BigDecimal projectHorMx;

    /**
     * Suite des triplets (horodate, projec_hor, val)
     */
    @JsonProperty("donPrevCt")
    private DonneesPrevisionCtWrapper donneesPrevCt;

    /**
     * Date heure de la composition de la prévision météo
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime datePrevMete;

    /**
     * Date heure de l'autocorrection de la prévision météo
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private OffsetDateTime dateCorMete;

}
