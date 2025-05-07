package ca.qc.hydro.epd.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.qc.hydro.epd.utils.UtcLocalDateTimeJsonSerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditableEntity extends AbstractEntity {

    @Column(name = "USAGER_CREA", nullable = false)
    @CreatedBy
    private String createdBy;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_CREA", nullable = false, columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate = LocalDateTime.now(ZoneOffset.UTC).withNano(0);

    @Column(name = "USAGER_DERMAJ", nullable = false)
    @LastModifiedBy
    private String lastModifiedBy;

    @JsonSerialize(using = UtcLocalDateTimeJsonSerializer.class)
    @Column(name = "DATE_DERMAJ", nullable = false, columnDefinition = "TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC).withNano(0);

}
