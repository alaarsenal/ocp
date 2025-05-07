package ca.qc.hydro.epd.domain;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public record PrevisionRedshift(
        String idCalc,
        LocalDateTime submitDate,
        String typeCalc,
        LocalDateTime dateUtc,
        @Nullable
        Integer timeStep,
        @Nullable
        Integer stepSpan,
        String probabilite,
        String codePoint,
        @Nullable
        String codeModele,
        @Nullable
        Double prevMw,
        @Nullable
        Double autoCorrectionMw,
        @Nullable
        Double prevNp,
        @Nullable
        Double prevMvar,
        @Nullable
        Integer codEqual,
        @Nullable
        String details
        ) {

        public PrevisionRedshift (String idCalc, LocalDateTime submitDate, String typeCalc, LocalDateTime dateUtc, String probabilite, String codePoint, Double prevMw) {
            this(idCalc, submitDate, typeCalc, dateUtc, null, null, probabilite, codePoint, null, prevMw, null, null, null, null, null);
        }
}
