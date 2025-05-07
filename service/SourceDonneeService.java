package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.SourceDonnee;
import ca.qc.hydro.epd.repository.SourceDonneeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SourceDonneeService {

    private final SourceDonneeRepository sourceDonneeRepository;

    public List<SourceDonnee> getAll() {
        return new ArrayList<>(sourceDonneeRepository.findAll());
    }
}
