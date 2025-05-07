package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.LocalDateTimeDeserializer;
import ca.qc.hydro.epd.utils.LocalDateTimeSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class HorodateurOutputBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String horodate;
    private String pasDeTemps;
    private String indHeureAvancee;
    private String val;

    @JsonProperty("minute_crx")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateHeureCreux;
    @JsonProperty("minute_pte")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateHeurePointe;
    @JsonProperty("pte_prevue")
    private Double maxPuissanceMinute;
    @JsonProperty("crx_prevu")
    private Double minPuissanceMinute;
    @JsonProperty("eng_prevue")
    private Double totPuissanceMinute;
    @JsonProperty("projec_hor")
    private String projectionHoraire;

    public HorodateurOutputBean() {

    }

    public HorodateurOutputBean(String horodate, String val) {
        this.horodate = horodate;
        this.val = val;
    }

    public HorodateurOutputBean(int pasDeTemps, boolean indHeureAvancee, String val) {
        this.pasDeTemps = pasDeTemps + "";
        this.indHeureAvancee = indHeureAvancee ? "1" : "0";
        this.val = val;
    }

}
