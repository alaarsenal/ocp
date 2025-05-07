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
public class PrevisionPlusRecentCalcRequest implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = 6260621435234482215L;

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

    @JsonProperty("niv")
    private String niveau;

    /**
     * Liste de codes de modèle de calcul de prévision
     */
    @Builder.Default
    @JsonProperty("listCodModele")
    private List<String> codesModele = new ArrayList<>();

    /**
     * Liste de codes de fonction de calcul de prévision
     */
    @Builder.Default
    @JsonProperty("listCodFonc")
    private List<String> codesFonc = new ArrayList<>();

    /**
     * Liste de code de types de prévision (optionnel, pas de défaut)
     */
    @Builder.Default
    @JsonProperty("listCodTypPrev")
    private List<String> codesTypePrev = new ArrayList<>();

    /**
     * Liste de codes de produit de prévision
     */
    @Builder.Default
    @JsonProperty("listCodProduitPrev")
    private List<String> codesProduitPrev = new ArrayList<>();

    /**
     * Liste de codes d'étiquette de prévision
     */
    @Builder.Default
    @JsonProperty("listCodeEtiq")
    private List<String> codesEtiq = new ArrayList<>();

    /**
     * Liste de code statut (optionnelle, 'COMP' par défaut)
     */
    @Builder.Default
    @JsonProperty("listCodStatutPrev")
    private List<String> codesStatutPrev = new ArrayList<>();

    /**
     * Code de fournisseur météo (optionnel, pas de défaut)
     */
    @JsonProperty("fournPrevMete")
    private String fournPrevMete;

}
