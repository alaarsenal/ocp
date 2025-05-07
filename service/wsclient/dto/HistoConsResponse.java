package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
@JsonTypeName("ConsommationsLire.rep")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class HistoConsResponse implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -354112487018839499L;

    @Builder.Default
    @JsonProperty("infoConsommations")
    private List<HistoConsResult> results = new ArrayList<>();

    @JsonProperty("messagesRetour")
    private MessagesRetourWrapper messagesRetourWrapper;

}
