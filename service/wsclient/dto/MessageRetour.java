package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

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
public class MessageRetour implements Serializable {

    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = -4638958079008938693L;

    private String texteMessage;
    private String noMessage;
    private String typeMessage;

    public String getMessageContent() {
        return StringUtils.join(new Object[]{this.typeMessage, this.noMessage, this.texteMessage}, ';');
    }
}
