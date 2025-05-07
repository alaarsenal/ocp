package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.TypePrevision;
import ca.qc.hydro.epd.repository.TypePrevisionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TypePrevisionService {

    private final TypePrevisionRepository typePrevisionRepository;

    public List<TypePrevision> getAll() {
        return new ArrayList<>(typePrevisionRepository.findAll());
    }

}
