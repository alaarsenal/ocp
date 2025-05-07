package ca.qc.hydro.epd.domain;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public record ConsbrutRedshift (
    @Nullable
    String fileUri,
    @Nullable
    String provider,
    @Nullable
    LocalDateTime dateUtc,
    @Nullable
    Integer minute,
    Boolean isHae,
    String codePt,
    @Nullable
    Double cons,
    @Nullable
    Integer codeEqual,
    @Nullable
    LocalDateTime datemaj,
    @Nullable
    String idChargement) {

    public ConsbrutRedshift(LocalDateTime dateUtc, Integer minute, String codePt, Double cons) {
        this(null, null, dateUtc, minute, null, codePt, cons, null, null, null);
    }
}
