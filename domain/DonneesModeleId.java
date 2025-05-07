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
public class DonneesModeleId implements Serializable {

    private Long pointId;

    private String codeModele;

    private String typeDonnee;

    private LocalDateTime dateEnrEffective;

    private LocalDateTime dateDebEffective;

}
