package ca.qc.hydro.epd.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.ActionAttribut;
import ca.qc.hydro.epd.domain.AttributJournal;
import ca.qc.hydro.epd.repository.ActionAttributRepository;
import ca.qc.hydro.epd.repository.AttributJournalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttributJournalService {

    private final AttributJournalRepository attributJournalRepository;
    private final ActionAttributRepository actionAttributRepository;

    public List<AttributJournal> getAllAttributs() {
        return attributJournalRepository.findAll();
    }

    public List<ActionAttribut> getAllActionsAttributs() {
        return actionAttributRepository.findAll();
    }
}
