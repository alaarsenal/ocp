package ca.qc.hydro.epd.service;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.context.MessageSource;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.exception.ValidationException;

import lombok.Generated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceValidationUtils {

    /**
     * Private constructor The @Generated is there to prevent it to be use by Jacoco code coverage
     */
    @Generated
    private ServiceValidationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateDateRange(OffsetDateTime dateDeb, OffsetDateTime dateFin, long maxDateRange, MessageSource messageSource) throws ValidationException {
        if (dateFin.compareTo(dateDeb) < 0) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_END_DATE_MUST_BE_GREATER_THAN_START_DATE,
                    new Object[]{dateDeb, dateFin}, messageSource
            ));
        }
        Duration duration = Duration.between(dateFin, dateDeb);
        long days = Math.abs(duration.toDays());
        if (days > maxDateRange) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.PROFIL_DATE_RANGE_TOO_BIG, new Object[]{maxDateRange}, messageSource));
        }
    }
}
