package ca.qc.hydro.epd.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class HistoConsId implements Serializable {

    private LocalDateTime jourCons;

    private LocalDateTime dateEnr;

    private Long pointId;

    private String codeSource;

    private String typeCons;

    private String porteeCons;

}
