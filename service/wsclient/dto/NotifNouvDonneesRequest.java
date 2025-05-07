package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serializable;

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
public class NotifNouvDonneesRequest implements Serializable {

    private String codeProduit;
    private String codeUtEmettrice;

}
