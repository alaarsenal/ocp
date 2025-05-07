package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.PrevisionEtude;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.repository.PrevisionEtudeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrevisionEtudeService {

    private final PrevisionEtudeRepository previsionEtudeRepository;

    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public PrevisionEtude getByCode(String codePrevEtud) throws NotFoundException {
        Optional<PrevisionEtude> optPrevEtude = previsionEtudeRepository.findByCodeFetchVpConfigs(codePrevEtud);
        if (optPrevEtude.isPresent()) {
            log.debug("get :: " + optPrevEtude.get().toString());
            return optPrevEtude.get();
        }
        throw new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.PREV_ETUDE_NOT_FOUND, new Object[]{codePrevEtud}, messageSource));
    }

    @Transactional(readOnly = true)
    public List<String> getAllProprietaires() {
        return new ArrayList<>(previsionEtudeRepository.findAllProprietaires());
    }

    @Transactional(readOnly = true)
    public List<PrevisionEtude> searchByProprietaires(List<String> proprietaires) {
        return new ArrayList<>(previsionEtudeRepository.findAllByProprietaires(proprietaires));
    }
}
