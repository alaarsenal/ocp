package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.Borne;
import ca.qc.hydro.epd.repository.BorneRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BorneService {

    private final BorneRepository borneRepository;

    public List<Borne> getAll() {
        return new ArrayList<>(borneRepository.findAll());
    }
}
