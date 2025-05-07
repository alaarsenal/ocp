package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.domain.TypeDonInd;
import ca.qc.hydro.epd.repository.TypeDonIndRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TypeDonIndService {

    private final TypeDonIndRepository typeDonIndRepository;

    public List<TypeDonInd> getAll() {
        return new ArrayList<>(typeDonIndRepository.findAll());
    }

}
