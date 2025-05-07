package ca.qc.hydro.epd.domain;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;

public class LongToBigDecimalConverter implements AttributeConverter<Long, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Long attribute) {
        return attribute == null ? null : BigDecimal.valueOf(attribute.longValue());
    }

    @Override
    public Long convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? 0L : dbData.longValueExact();
    }

}
