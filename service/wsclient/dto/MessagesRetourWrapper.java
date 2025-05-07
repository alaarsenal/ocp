package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class MessagesRetourWrapper implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -17718171067832799L;

    @Builder.Default
    @JsonProperty("item")
    private List<MessageRetour> messagesRetour = new ArrayList<>();

    /**
     * Dummy constructor just to support the JSON format when there's no error.
     *
     * @param dummy Just a placeholder
     */
    public MessagesRetourWrapper(String dummy) {
        // Empty
    }
}
