package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.dao.HelloRedshiftDao;
import ca.qc.hydro.epd.domain.Point;
import ca.qc.hydro.epd.dto.ConsbrutDto;
import ca.qc.hydro.epd.dto.PageDto;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.exception.RedshiftQueryException;
import ca.qc.hydro.epd.exception.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HelloRedshiftService {

    private final PointService pointService;
    private final HelloRedshiftDao helloRedshiftDao;
    private final MessageSource messageSource;

    public List<ConsbrutDto> getConsbruts(LocalDateTime dateDebut, LocalDateTime dateFin, String codeRefPoint) throws RedshiftQueryException, ValidationException, NotFoundException {
        // valider les paramètres
        // exemple: la période entre dateDebut et dateFin ne doit pas dépasser 48 heures
        if (dateFin.isAfter(dateDebut.plusHours(48))) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_DATE_RANGE_TOO_BIG, new Object[]{2}, messageSource));
        }
        // ....
        // appeler le dao avec code point
        Point point = pointService.getOneByCodeRef(codeRefPoint);
        return helloRedshiftDao.findConsbruts(dateDebut, dateFin, point.getCode());
    }

    public Page<ConsbrutDto> getConsbruts(LocalDateTime dateDebut, LocalDateTime dateFin, String codeRefPoint, PageDto pageDto) throws RedshiftQueryException, ValidationException, NotFoundException {
        // valider les paramètres
        // exemple: la période entre dateDebut et dateFin ne doit pas dépasser 48 heures
        if (dateFin.isAfter(dateDebut.plusHours(48))) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.COMMON_DATE_RANGE_TOO_BIG, new Object[]{2}, messageSource));
        }
        // ....
        // appeler le dao avec code point
        Point point = pointService.getOneByCodeRef(codeRefPoint);
        return helloRedshiftDao.findConsbruts(dateDebut, dateFin, point.getCode(), pageDto);
    }
}
