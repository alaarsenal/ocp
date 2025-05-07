package ca.qc.hydro.epd.service;

import static ca.qc.hydro.epd.specification.JournalisationSpecifications.areParents;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.avecPoints;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.betweenDateDebutDateFin;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.globalSearch;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inActions;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inCategories;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inConseillers;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inGroupement;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inModels;
import static ca.qc.hydro.epd.specification.JournalisationSpecifications.inPoints;
import static org.springframework.data.jpa.domain.Specification.where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.Journalisation;
import ca.qc.hydro.epd.dto.ConseillerDto;
import ca.qc.hydro.epd.dto.JournalRequestDto;
import ca.qc.hydro.epd.dto.PageDto;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.JournalisationRepository;
import ca.qc.hydro.epd.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JournalisationService {

    private final JournalisationRepository journalisationRepository;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public Page<Journalisation> getPage(JournalRequestDto dto) {
        Specification<Journalisation> specification = where(
                areParents(dto.isKeepChildren())
                        .and(inCategories(dto.getCategories()))
                        .and(inActions(dto.getActions()))
                        .and(inConseillers(dto.getConseillers()))
                        .and(inGroupement(dto.getGroupement()))
                        .and(inPoints(dto.getPoints()))
                        .and(inModels(dto.getModels()))
                        .and(globalSearch(dto.getGlobalSearch()))
                        .and(betweenDateDebutDateFin(dto.getDateDebut(), dto.getDateFin())
                                .and(avecPoints(dto.isAvecPoints())))
        );

        PageDto pageDto = PageDto.builder().page(dto.getPage()).size(dto.getSize()).orders(dto.getOrders()).build();
        Pageable pageable = PaginationUtils.getPageable(pageDto);
        return journalisationRepository.findAll(specification, pageable);
    }

    public List<ConseillerDto> getConseillers() {
        return journalisationRepository.findAllConseillers();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Journalisation> create(List<Journalisation> journalisations) throws ValidationException {

        LocalDateTime now = LocalDateTime.now();
        for (Journalisation journal : journalisations) {
            journal.setDateEnreg(now);
            if (journal.getDateDebut().isAfter(journal.getDateFin())) {
                throw new ValidationException(
                        ApiMessageFactory.getError(ApiMessageCode.JOURNALISATION_DATES_NON_CONFORMES, new Object[]{journal}, messageSource));
            }

        }
        try {
            return journalisationRepository.saveAll(journalisations);
        } catch (Exception e) {
            throw new ValidationException(
                    ApiMessageFactory.getError(ApiMessageCode.JOURNALISATION_SAVE_ERROR, new Object[]{journalisations}, messageSource));
        }

    }

    public List<Journalisation> getJournalisationsEnfantsByParentId(Long id) throws ValidationException {
        List<Journalisation> listejournauxEnfants = new ArrayList<>();
        Journalisation journal = this.getJournalisation(id);
        return this.getJournalisationsEnfants(journal, listejournauxEnfants);
    }

    public List<Journalisation> getJournalisationsEnfants(Journalisation journal, List<Journalisation> journauxEnfants) throws ValidationException {
        List<Journalisation> enfants = journalisationRepository.findByParentId(journal.getId());

        if (journal.getParentId() == null) {
            journauxEnfants = new ArrayList<>();
            journauxEnfants.add(journal);
        }

        if (enfants.isEmpty()) {
            return journauxEnfants;
        } else {
            journauxEnfants.add(enfants.get(0));
            return this.getJournalisationsEnfants(enfants.get(0), journauxEnfants);
        }
    }

    public Journalisation getJournalisation(Long id) throws ValidationException {
        Optional<Journalisation> journalisationOptional = journalisationRepository.findById(id);

        if (journalisationOptional.isEmpty()) {
            throw new ValidationException(
                    ApiMessageFactory.getError(ApiMessageCode.JOURNALISATION_NOT_FOUND, new Object[]{journalisationOptional}, messageSource));
        }
        return journalisationOptional.get();
    }
}
