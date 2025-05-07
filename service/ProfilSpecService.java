package ca.qc.hydro.epd.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.domain.ProfilSpec;
import ca.qc.hydro.epd.repository.ProfilSpecRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProfilSpecService {

    private final ProfilSpecRepository profilSpecRepository;

    @Transactional(readOnly = true)
    public List<ProfilSpec> getAll() {
        return profilSpecRepository.findAll();
    }

}
