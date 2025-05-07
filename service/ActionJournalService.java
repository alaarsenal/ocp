package ca.qc.hydro.epd.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.ActionJournal;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.ActionJournalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActionJournalService {

    private final ActionJournalRepository actionJournalRepository;
    private final MessageSource messageSource;

    public List<ActionJournal> getAll() {
        return actionJournalRepository.findAll();
    }

    public ActionJournal getByCode(ActionJournal.EActionJournalCode code) throws ValidationException {
        return actionJournalRepository.findByCode(code).orElseThrow(
                () -> new ValidationException(ApiMessageFactory.getError(ApiMessageCode.ACTION_JOURNALISATION_NOT_FOUND, new Object[]{code}, messageSource))
        );
    }

}
