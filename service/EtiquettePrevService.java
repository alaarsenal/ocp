package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.EtiquettePrev;
import ca.qc.hydro.epd.repository.EtiquettePrevRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EtiquettePrevService {

    private final EtiquettePrevRepository etiquettePrevRepository;

    public List<EtiquettePrev> getAll() {
        return new ArrayList<>(etiquettePrevRepository.findAll());
    }

}
