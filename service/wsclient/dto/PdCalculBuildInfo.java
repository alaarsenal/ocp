package ca.qc.hydro.epd.service.wsclient.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.DateTimeFormat;
import ca.qc.hydro.epd.utils.LocalDateTimeDeserializer;
import ca.qc.hydro.epd.utils.LocalDateTimeSerializer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdCalculBuildInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String livraison;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(value = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    private String jiras;
    private String version;
    private String group;
    private String artifact;
    private String name;
    private String branch;

}
