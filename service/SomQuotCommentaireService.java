package ca.qc.hydro.epd.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.qc.hydro.epd.apierror.ApiMessageCode;
import ca.qc.hydro.epd.apierror.ApiMessageFactory;
import ca.qc.hydro.epd.domain.SomQuotCommentaire;
import ca.qc.hydro.epd.exception.ValidationException;
import ca.qc.hydro.epd.repository.SomQuotCommentaireRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SomQuotCommentaireService {

    private static final String DEFAULT_CODE_ETIQ = "NUL";

    private final SomQuotCommentaireRepository somQuotCommentaireRepository;
    private final MessageSource messageSource;

    @Transactional(rollbackFor = Exception.class)
    public SomQuotCommentaire create(SomQuotCommentaire somQuotCommentaire) throws ValidationException {

        if (StringUtils.isBlank(somQuotCommentaire.getEtiquette()))
            somQuotCommentaire.setEtiquette(DEFAULT_CODE_ETIQ);

        somQuotCommentaire.setDateEnr(LocalDateTime.now(ZoneOffset.UTC).withNano(0));

        try {
            return somQuotCommentaireRepository.save(somQuotCommentaire);
        } catch (Exception e) {
            throw new ValidationException(ApiMessageFactory.getError(ApiMessageCode.SOMMAIRE_COMMENTAIRE_SAVE_ERROR,
                    new Object[]{somQuotCommentaire}, messageSource
            ));
        }

    }

}
