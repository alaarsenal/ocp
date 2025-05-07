package ca.qc.hydro.epd.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToCharConverter implements AttributeConverter<Boolean, Character> {

    @Override
    public Character convertToDatabaseColumn(Boolean attribute) {
        return (attribute == null || !attribute.booleanValue()) ? 'N' : 'O';
    }

    @Override
    public Boolean convertToEntityAttribute(Character dbData) {
        return Character.valueOf('O').equals(dbData);
    }


}
