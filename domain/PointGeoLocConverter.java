package ca.qc.hydro.epd.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.qc.hydro.epd.exception.EpdServerException;

@Converter(autoApply = true)
public class PointGeoLocConverter implements AttributeConverter<PointGeoLoc, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(PointGeoLoc attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new EpdServerException(e.getMessage());
        }
    }

    @Override
    public PointGeoLoc convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, PointGeoLoc.class);
        } catch (Exception e) {
            throw new EpdServerException(e.getMessage());
        }
    }

}
