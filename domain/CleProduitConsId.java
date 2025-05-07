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

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-04
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class CleProduitConsId implements Serializable {

    private Long pointId;

    private String codeSourceDonnee;

    private String codeProduit;

    private String noCle;

    private LocalDateTime dateEnr;

}
