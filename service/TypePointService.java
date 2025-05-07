package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.TypePoint;
import ca.qc.hydro.epd.repository.TypePointRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TypePointService {

    private final TypePointRepository typePointRepository;

    public List<TypePoint> getAll() {
        return new ArrayList<>(typePointRepository.findAll());
    }

}
