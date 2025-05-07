package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import ca.qc.hydro.epd.dao.PrevisionDatesCalculEtMeteoDao;
import ca.qc.hydro.epd.dao.PrevisionHoraireDao;
import ca.qc.hydro.epd.dao.TemperatureDorvalDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.PrevisionBqDonnees;
import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour1ModelDto;
import ca.qc.hydro.epd.dto.PrevisionBqMeteoHeureCalculRetour2ModelDto;
import ca.qc.hydro.epd.dto.PrevisionHoraireDto;
import ca.qc.hydro.epd.dto.ValeursMeteoDto;
import ca.qc.hydro.epd.enums.ETypeCalcul;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.utils.DatePrevisionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour les prévisions horaires
 *
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2021-12-06
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PrevisionHoraireService {

    @Value("${hq.pdcalcul.previsions-horaires.tolerance-date-calcul-minutes-moins:40}")
    private int toleranceDateCalculPrecedent;
    @Value("${hq.pdcalcul.previsions-horaires.tolerance-date-calcul-minutes-plus:0}")
    private int toleranceDateCalculSuivant;


    public static final String TYPE_PREVISION_RAFF = ETypeCalcul.RAFF.getCode();
    private static final List<String> typePrevisionRte1 = Lists.newArrayList(ETypeCalcul.PPCT.getCode(), ETypeCalcul.CYCL.getCode());
    private final PrevisionHoraireDao previsionHoraireDao;
    private final TemperatureDorvalDao temperatureDorvalDao;
    private final PrevisionDatesCalculEtMeteoDao previsionDatesCalculEtMeteoDao;
    private final PointService pointService;

    /**
     * Retourne la liste des prévisions horaires (incluant la consommation réelle et la valeur de temperature réel)
     *
     * @param dateRef    date de référence
     * @param projection le nombre de minutes à soustraire de la datePrevue (HNE) pour trouver la bonne date de calcul
     * @return la liste des prévisions horaires (incluant la consommation réelle)
     */
    public PrevisionBqDonnees getPrevisionsHorairesPeriode(LocalDateTime dateRef, Integer projection, int nombreHeures, String codeRefPoint) throws ExecutionException, InterruptedException, NotFoundException {
        ZonedDateTime startDateMontreal = dateRef
                .atZone(ZoneId.of(DatePrevisionUtil.MONTREAL))
                .minusDays(1).withHour(0).withMinute(1);
        LocalDateTime dateRefAjuste = LocalDateTime.ofInstant(startDateMontreal.toInstant(), ZoneOffset.UTC);

        PrevisionBqDonnees pbq = new PrevisionBqDonnees();
        PrevisionBqMeteoHeureCalculRetour2ModelDto previsonrte2 = null;
        StopWatch stopWatch = StopWatch.createStarted();

        Point point = pointService.getOneByCodeRef(codeRefPoint);

        CompletableFuture<List<PrevisionHoraireDto>> consommationReelleCompletableFuture = CompletableFuture.supplyAsync(() -> previsionHoraireDao.findConsommationReellePeriode(dateRefAjuste, nombreHeures, point.getId()));
        CompletableFuture<List<PrevisionHoraireDto>> previsionPeriodeCompletableFuture = CompletableFuture.supplyAsync(() -> previsionHoraireDao.findPrevisionPeriode(dateRefAjuste, dateRef, projection, nombreHeures, point.getId()));
        CompletableFuture<List<PrevisionHoraireDto>> indiceNpCompletableFuture = CompletableFuture.supplyAsync(() -> previsionHoraireDao.findIndiceNpPeriode(dateRefAjuste, dateRef, projection, nombreHeures, point.getId()));

        List<PrevisionHoraireDto> consommationReellePeriode = consommationReelleCompletableFuture.get();
        List<PrevisionHoraireDto> previsionPeriode = previsionPeriodeCompletableFuture.get();
        List<PrevisionHoraireDto> indiceNpPeriode = indiceNpCompletableFuture.get();

        previsionPeriode.forEach(
                previsionHoraireDto -> {
                    // ajout des consommations réelles
                    PrevisionHoraireDto consommationReelle = consommationReellePeriode.stream()
                            .filter(prev -> prev.getDatePrevue().equals(previsionHoraireDto.getDatePrevue())).findFirst().orElse(null);
                    if (Objects.nonNull(consommationReelle)) {
                        previsionHoraireDto.setMinutePointeReel(consommationReelle.getMinutePointeReel());
                        previsionHoraireDto.setValeurPointeReel(consommationReelle.getValeurPointeReel());
                        previsionHoraireDto.setValeurHeureReel(consommationReelle.getValeurHeureReel());
                    }

                    // ajout des indices np
                    PrevisionHoraireDto indicesNp = indiceNpPeriode.stream()
                            .filter(prev -> prev.getDatePrevue().equals(previsionHoraireDto.getDatePrevue())).findFirst().orElse(null);
                    if (Objects.nonNull(indicesNp)) {
                        previsionHoraireDto.setIndiceNp(indicesNp.getValeurPointePrevu());
                    }

                }
        );

        // ajout des consommations réelles avec des dates non trouvées dans la liste des consommations prévues
        previsionPeriode.addAll(consommationReellePeriode.stream()
                .filter(element -> previsionPeriode.stream().noneMatch(prev -> element.getDatePrevue().equals(prev.getDatePrevue())))
                .toList());

        // ajout des heures inexistantes dans le réel et le prévu
        List<LocalDateTime> heures = new ArrayList<>();
        for (int i = 1; i <= nombreHeures; i++) {
            heures.add(dateRefAjuste.plusHours(i).withMinute(0).withSecond(0));
        }
        heures.forEach(
                heure -> {
                    PrevisionHoraireDto previsionHoraireDto = previsionPeriode.stream().filter(prev -> heure.equals(prev.getDatePrevue())).findFirst().orElse(null);
                    if (Objects.isNull(previsionHoraireDto)) {
                        previsionPeriode.add(PrevisionHoraireDto.builder().datePrevue(heure).build());
                    }
                }
        );

        // APDTE-1327
        PrevisionBqMeteoHeureCalculRetour1ModelDto previsonrte1 = previsionDatesCalculEtMeteoDao.findPrevisionPlusRecentePremierRetour(dateRef, point.getId());

        // Le système détermine la date heure de calcul et la date heure d'autocorrection du calcul en fonction de la date heure (dteHreCalcRet1) et du type (typPrevRet1) de la prévision identifiée ci-dessus.
        if (previsonrte1 != null) {
            previsonrte2 = previsionDatesCalculEtMeteoDao.findPrevisionDatehreEtAutoCalculDeuxiemeRetour(previsonrte1, point.getId());
        }

        // Si le type de cette prévision (typPrevRet1) est "CYCL" ou "PPCT":
        // APDTE-1327 :: 4.1
        List<ValeursMeteoDto> temperatureslist = new ArrayList<>();
        if (null != previsonrte2 && typePrevisionRte1.contains(previsonrte1.getTypPrevRet1())) {
            // la date heure du calcul (dteHreCalc2) et la date heure d'autocorrection du calcul (dteHreAutoCor2) sont égales toutes les deux à (dteHreCalcRet1).
            pbq.setDateCalcul(previsonrte1.getDteHreCalcRet1());
            pbq.setDateCalculAutoCorrelation(previsonrte1.getDteHreCalcRet1()); // AVOIR AVEC OLIVIER PAS de CHAMPS CALC_AUTO_CORR
            // la date heure de la météo (dteHrePrevMete) est égale à  (dteHrePrevMeteRet1).
            pbq.setDateEmissionMeteo(previsonrte1.getDteHrePrevMeteRet1());
            // la date heure d'autocorrection (dteHreAutoCorMete) de la météo est égale à (dteHreAutoCorMeteRet1).
            pbq.setDateEmissionMeteoAutoCorrection(previsonrte1.getDteHreAutoCorMeteRet1());

            //APDTE-1328 :: 5  Le système invoque le Backend "Colonne Températures Dorval"
            temperatureslist = temperatureDorvalDao.findTemperaturesMeteoPontDorval(pbq.getDateCalcul(), pbq.getDateCalcul().plusHours(nombreHeures), pbq.getDateEmissionMeteo(), pbq.getDateEmissionMeteoAutoCorrection());

        }
        // APDTE-1327 :: 4.2.2
        // 4.2- Si le type de cette prévision est "RAFF":
        if (null != previsonrte2 && previsonrte1.getTypPrevRet1().equalsIgnoreCase(TYPE_PREVISION_RAFF)) {
            // la date heure du calcul (dteHreCalc) est égale à (dteHreCalcRet2).
            pbq.setDateCalcul(previsonrte2.getDateHreCalcRet2());
            // la date heure d'autocorrection du calcul (dteHreAutoCor) est égale à (dteHreCalcRet1).
            pbq.setDateCalculAutoCorrelation(previsonrte1.getDteHreCalcRet1());  // AVOIR AVEC OLIVIER PAS de CHAMPS CALC_AUTO_CORR
            // la date heure de la météo (dteHrePrevMete) est égale à  (dteHrePrevMeteRet2).
            pbq.setDateEmissionMeteo(previsonrte2.getDateHrePrevMeteRet2());
            // la date heure d'autocorrection (dteHreAutoCorMete) de la météo est égale à  (dteHreAutoCorMeteRet2).
            pbq.setDateEmissionMeteoAutoCorrection(previsonrte2.getDateHreAutoCorMeteRet2());

            // APDTE-1328 :: 5  Le système invoque le Backend "Colonne Températures Dorval"
            temperatureslist = temperatureDorvalDao.findTemperaturesMeteoPontDorval(pbq.getDateCalcul(), pbq.getDateCalcul().plusHours(nombreHeures), pbq.getDateEmissionMeteo(), pbq.getDateEmissionMeteoAutoCorrection());
        }

        // ajout de la valeur Temperature
        this.getPrevisionsHorairesPeriodeAvecTemperature(previsionPeriode, temperatureslist);

        List<ZonedDateTime> jours = previsionPeriode.stream()
                .map(prev -> DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue())
                        .minusHours(1).truncatedTo(ChronoUnit.DAYS))
                .distinct()
                .toList();

        List<PrevisionHoraireDto> previsionPeriodeOrdered = previsionPeriode
                .stream()
                .sorted(Comparator.comparing(PrevisionHoraireDto::getDatePrevue))
                .toList();

        detecterPointesParJour(previsionPeriodeOrdered, jours);
        detecterCreuxParJour(previsionPeriodeOrdered, jours);
        detecterFinJour(previsionPeriodeOrdered, jours);
        detecterHeureActuelle(previsionPeriodeOrdered);

        log.debug("Temps d'exécution getPrevisionsHorairesPeriode : {} ms", stopWatch.getTime());

        // retour de la liste triée par datePrevue
        pbq.setPrevisions(previsionPeriodeOrdered);

        return pbq;
    }

    public PrevisionHoraireDto getPrevisionPourDatePrevueAvecTolerance(LocalDateTime datePrevue, Integer projection, String codeRefPoint) throws ExecutionException, InterruptedException, NotFoundException {
        var dateCalcul = datePrevue.minusMinutes(projection);
        ZonedDateTime startDateMontreal = datePrevue
                .atZone(ZoneId.of(DatePrevisionUtil.MONTREAL))
                .withHour(0).withMinute(1);
        LocalDateTime jourPrevu = LocalDateTime.ofInstant(startDateMontreal.toInstant(), ZoneOffset.UTC);

        int tolerancePlus = toleranceDateCalculSuivant > projection ? projection : toleranceDateCalculSuivant;

        Point point = pointService.getOneByCodeRef(codeRefPoint);

        CompletableFuture<List<PrevisionHoraireDto>> previsionsCompletableFuture = CompletableFuture.supplyAsync(
                () -> previsionHoraireDao.findPrevisionPourDatePrevueAvecTolerance(datePrevue, dateCalcul, jourPrevu, point.getId(), toleranceDateCalculPrecedent, tolerancePlus));
        CompletableFuture<List<PrevisionHoraireDto>> indicesNpCompletableFuture = CompletableFuture.supplyAsync(
                () -> previsionHoraireDao.findIndiceNpPourDatePrevueAvecTolerance(datePrevue, dateCalcul, jourPrevu, point.getId(), toleranceDateCalculPrecedent, tolerancePlus));

        List<PrevisionHoraireDto> previsions = previsionsCompletableFuture.get();
        List<PrevisionHoraireDto> indicesNp = indicesNpCompletableFuture.get();

        LocalDateTime dateCalculPlusProche = previsions.stream()
                .map(PrevisionHoraireDto::getDateCalcul)
                .min(Comparator.comparingLong(dt -> ChronoUnit.MINUTES.between(dt, dateCalcul)))
                .orElse(null);

        if (Objects.isNull(dateCalculPlusProche)) {
            return null;
        }

        Optional<PrevisionHoraireDto> previsionOpt = previsions.stream().filter(prev -> dateCalculPlusProche.equals(prev.getDateCalcul())).findFirst();
        Optional<PrevisionHoraireDto> indicNpOpt = indicesNp.stream().filter(prev -> dateCalculPlusProche.equals(prev.getDateCalcul())).findFirst();

        PrevisionHoraireDto resultat = PrevisionHoraireDto.builder()
                .datePrevue(datePrevue)
                .dateCalcul(dateCalculPlusProche)
                .build();
        if (previsionOpt.isPresent()) {
            var prevision = previsionOpt.get();
            resultat.setMinutePointePrevu(prevision.getMinutePointePrevu());
            resultat.setValeurPointePrevu(prevision.getValeurPointePrevu());
            resultat.setValeurHeurePrevu(prevision.getValeurHeurePrevu());
        }
        if (indicNpOpt.isPresent()) {
            var prevision = indicNpOpt.get();
            resultat.setIndiceNp(prevision.getValeurPointePrevu());
        }

        return resultat;
    }

    public void getPrevisionsHorairesPeriodeAvecTemperature(List<PrevisionHoraireDto> previsions, List<ValeursMeteoDto> previsionAvecTemp) {

        if (null != previsionAvecTemp) {
            for (ValeursMeteoDto valeur : previsionAvecTemp) {
                for (PrevisionHoraireDto prevision : previsions) {
                    if (valeur.getHorodate().isEqual(prevision.getDatePrevue())) {
                        prevision.setTemperature(valeur.getValeur());
                    }
                }
            }
        }
    }


    private void detecterPointesParJour(List<PrevisionHoraireDto> previsionPeriode, List<ZonedDateTime> jours) {
        // isValeurPointePrevueAm
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurPointePrevu()) && DatePrevisionUtil.memeJour(jour, datePrevue) && DatePrevisionUtil.avantMidi(datePrevue);
                        })
                        .max(Comparator.comparing(PrevisionHoraireDto::getValeurPointePrevu))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsValeurPointePrevueAm(Boolean.TRUE))
        );
        // isValeurPointePrevuePm
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurPointePrevu()) && DatePrevisionUtil.memeJour(jour, datePrevue) && DatePrevisionUtil.apresMidi(datePrevue);
                        })
                        .max(Comparator.comparing(PrevisionHoraireDto::getValeurPointePrevu))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsValeurPointePrevuePm(Boolean.TRUE))
        );
        // isValeurPointeReelAm
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurPointeReel()) && DatePrevisionUtil.memeJour(jour, datePrevue) && DatePrevisionUtil.avantMidi(datePrevue);
                        })
                        .max(Comparator.comparing(PrevisionHoraireDto::getValeurPointeReel))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsValeurPointeReelAm(Boolean.TRUE))
        );
        // isValeurPointeReelPm
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurPointeReel()) && DatePrevisionUtil.memeJour(jour, datePrevue) && DatePrevisionUtil.apresMidi(datePrevue);
                        })
                        .max(Comparator.comparing(PrevisionHoraireDto::getValeurPointeReel))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsValeurPointeReelPm(Boolean.TRUE))
        );
    }

    private void detecterCreuxParJour(List<PrevisionHoraireDto> previsionPeriode, List<ZonedDateTime> jours) {
        // isCreuxPrevu
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurHeurePrevu()) && DatePrevisionUtil.memeJour(jour, datePrevue);
                        })
                        .min(Comparator.comparing(PrevisionHoraireDto::getValeurHeurePrevu))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsCreuxPrevue(Boolean.TRUE))
        );
        // isCreuxReel
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> {
                            ZonedDateTime datePrevue = DatePrevisionUtil.getDatePrevueAsZonedDateTime(prev.getDatePrevue());
                            return Objects.nonNull(prev.getValeurHeureReel()) && DatePrevisionUtil.memeJour(jour, datePrevue);
                        })
                        .min(Comparator.comparing(PrevisionHoraireDto::getValeurHeureReel))
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsCreuxReel(Boolean.TRUE))
        );
    }

    private void detecterFinJour(List<PrevisionHoraireDto> previsionPeriode, List<ZonedDateTime> jours) {
        jours.forEach(
                jour -> previsionPeriode.stream()
                        .filter(prev -> DatePrevisionUtil.finJour(jour, prev.getDatePrevue()))
                        .findFirst()
                        .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsEndOfDay(Boolean.TRUE))
        );
    }

    private void detecterHeureActuelle(List<PrevisionHoraireDto> previsionPeriode) {
        previsionPeriode.stream()
                .filter(prev -> prev.getDatePrevue().truncatedTo(ChronoUnit.HOURS).equals(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS)))
                .findFirst()
                .ifPresent(previsionHoraireDto -> previsionHoraireDto.setIsCurrentHour(Boolean.TRUE));

    }

}
