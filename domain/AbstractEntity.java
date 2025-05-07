package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.UtcLocalDateTimeJsonSerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {

    @Column(name = "USAGER_MAJ", nullable = false)
    private String usagerMaj;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_MAJ", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime dateMaj;

}
