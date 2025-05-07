package ca.qc.hydro.epd.service.wsclient.dto;


import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonRootName("Reponse")
@JsonTypeName("PrevisionPointCtMtHor.rep")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class PrevisionCtMtHorResponse extends RestJsonOutputBean {

    private String codProduitPrev;

    private String debPeriodePrev;

    private String finPeriodePrev;

    private List<PrevPoint> prevPoint;

    @Data
    @Builder
    @ToString
    public static class PrevPoint {

        private long idPoint;

        @NonNull
        private String codPoint;

        @NonNull
        private String codRefPoint;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private EnTeteCt enTeteCt;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private EnTeteMt enTeteMt;

        private HorodateurOutputBeanWrapper<HorodateurOutputBean> prevPointCtMt;

    }

    @Data
    @Builder
    public static class EnTeteCt {
        @NonNull
        private String codModele;
        @NonNull
        private String codFonction;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime dateHreCalcRaff;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime debPeriodePrevRaff;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime dateHreCalcCycl;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime debPeriodePrevCycl;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime dateHreCalcPpct;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime debPeriodePrevPpct;

        @JsonIgnore
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime jourFinPrev;
    }

    @Data
    @Builder
    public static class EnTeteMt {
        private long calcMtId;
        @NonNull
        private String codModele;
        @NonNull
        private String codFonction;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime dateHreCalc;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private OffsetDateTime debPeriodePrev;

    }

    @Override
    public String toString() {
        return "PrevisionCtMtHorResponse{" +
                "codProduitPrev='" + codProduitPrev + '\'' +
                ", debPeriodePrev='" + debPeriodePrev + '\'' +
                ", finPeriodePrev='" + finPeriodePrev + '\'' +
                ", messagesRetour=" + getMessagesRetour() +
                '}';
    }
}
