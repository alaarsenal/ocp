package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.Produit;
import ca.qc.hydro.epd.domain.ProduitId;
import ca.qc.hydro.epd.exception.NotFoundException;
import ca.qc.hydro.epd.repository.ProduitRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Khaled Ghali
 * @version 1.0.0
 * @since 2022-03-25
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final MessageSource messageSource;

    /**
     * Retourne la liste de tous les produits.
     *
     * @return Une {@link List} de {@link ca.qc.hydro.epd.domain.Produit}
     */
    public List<Produit> getAll() {
        return new ArrayList<>(produitRepository.findAll());
    }

    /**
     * Chercher un produit par son id
     *
     * @param id id à chercher
     * @return produit
     * @throws NotFoundException exception si le produit n'existe pas
     */
    public Produit getOne(ProduitId id) throws NotFoundException {
        return produitRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ApiMessageFactory.getError(ApiMessageCode.PRODUIT_NOT_FOUND, new Object[]{id}, messageSource))
        );
    }

}
