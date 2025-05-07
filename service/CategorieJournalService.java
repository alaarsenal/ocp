package ca.qc.hydro.epd.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.CategorieJournal;
import ca.qc.hydro.epd.repository.CategorieJournalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategorieJournalService {

    private final CategorieJournalRepository categorieJournalRepository;

    public List<CategorieJournal> getAll() {
        return categorieJournalRepository.findAll();
    }
}
