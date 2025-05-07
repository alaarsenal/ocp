package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.domain.Modele;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.repository.PointModeleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ModeleService {

    private final PointModeleRepository pointModeleRepository;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public List<Modele> getModelesByGroupement(String groupement, Boolean indiceConf) {
        List<Long> pointsIds = pointService.getPointsByGrp(Collections.singletonList(groupement)).stream().map(Point::getId).toList();
        if (Boolean.TRUE.equals(indiceConf)) {
            return new ArrayList<>(pointModeleRepository.findModelesByPointsAndIndConfig('O', pointsIds));
        }
        return pointModeleRepository.findByPointId(pointsIds);
    }

    @Transactional(readOnly = true)
    public List<Modele> getModelesByPoints(List<String> codesRefPoints) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return pointModeleRepository.findByPointId(pointsIds);
    }

    /**
     * Retourne la liste de tous les modèles corrigeables par point.
     *
     * @return Une {@link List} de {@link ca.qc.hydro.epd.domain.Modele}
     */
    public List<Modele> getModelesCorrigeablesByPoints(List<String> codesRefPoints) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return new ArrayList<>(pointModeleRepository.findModelesByPointsAndIndConfig('O', pointsIds));
    }

    /**
     * Retourne la liste de tous les modèles pondérables par point. DISCLAIMER - Pour l'instant, la liste des modèles
     * pondérables sera la même que les modèles configurables et/ou corrigeables. Ça ne répond pas au besoin du client,
     * mais c'est une décision qui a été prise par l'équipe TI.
     *
     * @return Une {@link List} de {@link ca.qc.hydro.epd.domain.Modele}
     */
    public List<Modele> getModelesPondByPoints(String groupement) {
        List<Long> pointsIds = pointService.getPointsByGrp(Collections.singletonList(groupement)).stream().map(Point::getId).toList();
        return new ArrayList<>(pointModeleRepository.findModelesByPointsAndIndConfig('O', pointsIds));
    }

}
