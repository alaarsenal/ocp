package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.UtcLocalDateTimeJsonSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "PDC805_ETIQUETTE_PREV")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EtiquettePrev extends AbstractEntity {

    @Id
    @Column(name = "CODE_ETIQ", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "NOM_ETIQ", nullable = false)
    private String nom;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "HEURE_ETIQ", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime heure;

    @Column(name = "DESC_ETIQ", nullable = false)
    private String description;

}
