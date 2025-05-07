package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.MeilleurePrevisionDto;
import ca.qc.hydro.epd.dto.MeilleurePrevisionResponseDto;
import ca.qc.hydro.epd.enums.ETypeCalcul;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.WebClientException;
import ca.qc.hydro.epd.service.wsclient.PrevisionCtMtHorService;
import ca.qc.hydro.epd.service.wsclient.dto.PrevisionCtMtHorResponse;
import ca.qc.hydro.epd.utils.DatePrevisionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MeilleurePrevisionService {

    private static final String BQ = "BQ";
    private static final String AUTOCOR = "autocor";

    private final PrevisionCtMtHorService previsionCtMtHorService;
    private final PointService pointService;

    public MeilleurePrevisionResponseDto getPrevisions(OffsetDateTime dateDebut, OffsetDateTime datefin, String codeRefPoint) throws WebClientException, NotFoundException {
        var result = new MeilleurePrevisionResponseDto();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC)
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter horodateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        PrevisionCtMtHorResponse prevResCtMtHor;

        Point point = pointService.getOneByCodeRef(codeRefPoint);
        var objectif = BQ.equals(codeRefPoint) ? codeRefPoint + " " + AUTOCOR : point.getCode();

        prevResCtMtHor = previsionCtMtHorService.getPrevCtMtHor(dtf.format(dateDebut), dtf.format(datefin), objectif);

        // Ajout du retour de PdCalcul dans la réponse pour des fins de débogage.
        result.setPrevisionCtMtHorResponse(prevResCtMtHor);

        PrevisionCtMtHorResponse.PrevPoint previsions = null;

        if (Objects.nonNull(prevResCtMtHor) && Objects.nonNull(prevResCtMtHor.getPrevPoint())) {
            previsions = prevResCtMtHor.getPrevPoint().stream()
                    .filter(prevPoint -> codeRefPoint.equals(prevPoint.getCodRefPoint()))
                    .findFirst()
                    .orElse(null);
        }

        if (Objects.isNull(previsions)) {
            return result;
        }

        PrevisionCtMtHorResponse.EnTeteCt enTeteCt = previsions.getEnTeteCt();
        PrevisionCtMtHorResponse.EnTeteMt enTeteMt = previsions.getEnTeteMt();

        result.setPrevisions(previsions.getPrevPointCtMt().getValeurs().stream().map(prevision -> {
            var datePrevision = LocalDateTime.parse(prevision.getHorodate(), horodateFormatter);
            MeilleurePrevisionDto meilleurePrevisionDto = MeilleurePrevisionDto.builder()
                    .datePrevision(datePrevision)
                    .valeur(Double.valueOf(prevision.getVal()))
                    .build();
            addTypeCalculAndDateCalcul(meilleurePrevisionDto, enTeteCt, enTeteMt);
            return meilleurePrevisionDto;
        }).toList());

        List<ZonedDateTime> jours = result.getPrevisions().stream()
                .map(prev -> DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevision())
                        .minusHours(1).truncatedTo(ChronoUnit.DAYS))
                .distinct()
                .toList();

        detecterFinJour(result.getPrevisions(), jours);
        detecterHeureActuelle(result.getPrevisions());
        detecterFinTypeCalcul(result.getPrevisions());

        return result;
    }

    private void addTypeCalculAndDateCalcul(MeilleurePrevisionDto meilleurePrevisionDto, PrevisionCtMtHorResponse.EnTeteCt enTeteCt, PrevisionCtMtHorResponse.EnTeteMt enTeteMt) {
        LocalDateTime datePrevision = meilleurePrevisionDto.getDatePrevision();

        LocalDateTime debPeriodePrevRaff = Objects.nonNull(enTeteCt) && Objects.nonNull(enTeteCt.getDebPeriodePrevRaff()) ? enTeteCt.getDebPeriodePrevRaff().toLocalDateTime() : null;
        LocalDateTime debPeriodePrevCycl = Objects.nonNull(enTeteCt) && Objects.nonNull(enTeteCt.getDebPeriodePrevCycl()) ? enTeteCt.getDebPeriodePrevCycl().toLocalDateTime() : null;
        LocalDateTime debPeriodePrevPpct = Objects.nonNull(enTeteCt) && Objects.nonNull(enTeteCt.getDebPeriodePrevPpct()) ? enTeteCt.getDebPeriodePrevPpct().toLocalDateTime() : null;
        LocalDateTime debPeriodePrevMt = Objects.nonNull(enTeteMt) && Objects.nonNull(enTeteMt.getDebPeriodePrev()) ? enTeteMt.getDebPeriodePrev().toLocalDateTime() : null;

        if (isCalculMt(enTeteMt, datePrevision, debPeriodePrevMt)) {
            meilleurePrevisionDto.setDateCalcul(enTeteMt.getDateHreCalc().toLocalDateTime());
            meilleurePrevisionDto.setTypeCalcul(ETypeCalcul.MT);
        } else if (isCalculPpct(enTeteCt, datePrevision, debPeriodePrevPpct)) {
            meilleurePrevisionDto.setDateCalcul(enTeteCt.getDateHreCalcPpct().toLocalDateTime());
            meilleurePrevisionDto.setTypeCalcul(ETypeCalcul.PPCT);
        } else if (isCalculCycl(enTeteCt, datePrevision, debPeriodePrevCycl)) {
            meilleurePrevisionDto.setDateCalcul(enTeteCt.getDateHreCalcCycl().toLocalDateTime());
            meilleurePrevisionDto.setTypeCalcul(ETypeCalcul.CYCL);
        } else if (isCalculRaff(enTeteCt, datePrevision, debPeriodePrevRaff)) {
            meilleurePrevisionDto.setDateCalcul(enTeteCt.getDateHreCalcRaff().toLocalDateTime());
            meilleurePrevisionDto.setTypeCalcul(ETypeCalcul.RAFF);
        }
    }

    private boolean isCalculRaff(PrevisionCtMtHorResponse.EnTeteCt enTeteCt, LocalDateTime datePrevision, LocalDateTime debPeriodePrevRaff) {
        return Objects.nonNull(debPeriodePrevRaff)
                && (datePrevision.isAfter(debPeriodePrevRaff) || datePrevision.isEqual(debPeriodePrevRaff))
                && Objects.nonNull(enTeteCt.getDateHreCalcRaff());
    }

    private boolean isCalculCycl(PrevisionCtMtHorResponse.EnTeteCt enTeteCt, LocalDateTime datePrevision, LocalDateTime debPeriodePrevCycl) {
        return Objects.nonNull(debPeriodePrevCycl)
                && (datePrevision.isAfter(debPeriodePrevCycl) || datePrevision.isEqual(debPeriodePrevCycl))
                && Objects.nonNull(enTeteCt.getDateHreCalcCycl());
    }

    private boolean isCalculPpct(PrevisionCtMtHorResponse.EnTeteCt enTeteCt, LocalDateTime datePrevision, LocalDateTime debPeriodePrevPpct) {
        return Objects.nonNull(debPeriodePrevPpct)
                && (datePrevision.isAfter(debPeriodePrevPpct) || datePrevision.isEqual(debPeriodePrevPpct))
                && Objects.nonNull(enTeteCt.getDateHreCalcPpct());
    }

    private boolean isCalculMt(PrevisionCtMtHorResponse.EnTeteMt enTeteMt, LocalDateTime datePrevision, LocalDateTime debPeriodePrevMt) {
        return Objects.nonNull(debPeriodePrevMt)
                && (datePrevision.isAfter(debPeriodePrevMt) || datePrevision.isEqual(debPeriodePrevMt))
                && Objects.nonNull(enTeteMt.getDateHreCalc());
    }

    private void detecterFinJour(List<MeilleurePrevisionDto> meilleurePrevisionDtos, List<ZonedDateTime> jours) {
        jours.forEach(
                jour -> meilleurePrevisionDtos.stream()
                        .filter(prev -> DatePrevisionUtil.finJour(jour, prev.getDatePrevision()))
                        .findFirst()
                        .ifPresent(meilleurePrevisionDto -> meilleurePrevisionDto.setFinJour(Boolean.TRUE))
        );
    }

    private void detecterHeureActuelle(List<MeilleurePrevisionDto> meilleurePrevisionDtos) {
        meilleurePrevisionDtos.stream()
                .filter(prev -> prev.getDatePrevision().truncatedTo(ChronoUnit.HOURS).equals(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS)))
                .findFirst()
                .ifPresent(meilleurePrevisionDto -> meilleurePrevisionDto.setHeureActuelle(Boolean.TRUE));
    }

    private void detecterFinTypeCalcul(List<MeilleurePrevisionDto> meilleurePrevisionDtos) {
        for (int i = 1; i < meilleurePrevisionDtos.size(); i++) {
            if (!(meilleurePrevisionDtos.get(i).getTypeCalcul().equals(meilleurePrevisionDtos.get(i - 1).getTypeCalcul()))) {
                meilleurePrevisionDtos.get(i - 1).setFinTypeCalcul(Boolean.TRUE);
            }
        }
    }

}
