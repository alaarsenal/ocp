package ca.qc.hydro.epd.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.domain.Fonction;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.repository.PointModeleFonctionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FonctionService {

    private final PointModeleFonctionRepository pointModeleFonctionRepository;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public List<Fonction> getFonctionsByPointsAndModeles(List<String> codesRefPoints, List<String> codesMod) {
        List<Long> pointsIds = pointService.getByCodesRef(codesRefPoints).stream().map(Point::getId).toList();
        return pointModeleFonctionRepository.findFonctionsByPointsAndModeles(pointsIds, codesMod);
    }

}
