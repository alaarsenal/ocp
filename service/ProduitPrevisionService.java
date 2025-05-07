package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.ProduitPrevision;
import ca.qc.hydro.epd.repository.ProduitPrevisionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProduitPrevisionService {

    private final ProduitPrevisionRepository produitPrevisionRepository;

    public List<ProduitPrevision> getAll() {
        return new ArrayList<>(produitPrevisionRepository.findAll());
    }
}
