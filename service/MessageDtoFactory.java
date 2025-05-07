package ca.qc.hydro.epd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import ca.qc.hydro.epd.apierror.ApiMessageLevel;
import ca.qc.hydro.epd.dto.CodeMessage;
import ca.qc.hydro.epd.dto.MessageDto;

public class MessageDtoFactory {

    private MessageDtoFactory() {
    }

    /**
     * Build a list of one api error field from received arguments.
     *
     * @param codeMessage   error code to return
     * @param level         message level (information, error or warning)
     * @param messageSource message source
     * @return list of one api error field
     */
    public static List<MessageDto> getMessage(CodeMessage codeMessage, ApiMessageLevel level, Object[] msgParams, MessageSource messageSource) {

        MessageDto apiMessage = new MessageDto();
        apiMessage.setLevel(level);
        apiMessage.setCode(codeMessage.getCode());

        if (messageSource != null) {
            apiMessage.setMessage(messageSource.getMessage("api.validator.msg." + codeMessage.name(), msgParams, codeMessage.getDefaultMessage(), LocaleContextHolder.getLocale()));
        } else {
            apiMessage.setMessage(codeMessage.getDefaultMessage());
        }

        List<MessageDto> messages = new ArrayList<>();
        messages.add(apiMessage);
        return messages;
    }

    /**
     * Return the message form the messageSource
     *
     * @param codeMessage
     * @param msgParams
     * @param messageSource
     * @return
     */
    public static String getMessage(CodeMessage codeMessage, Object[] msgParams, MessageSource messageSource) {

        if (messageSource != null) {
            return messageSource.getMessage("api.validator.msg." + codeMessage.name(), msgParams, codeMessage.getDefaultMessage(), LocaleContextHolder.getLocale());
        } else {
            return codeMessage.getDefaultMessage();
        }
    }

    /**
     * Build a list of one api error field from received arguments.
     *
     * @param codeMessage   error code to return
     * @param messageSource message source
     * @return list of one api error field
     */
    public static List<MessageDto> getWarning(CodeMessage codeMessage, Object[] msgParams, MessageSource messageSource) {
        return getMessage(codeMessage, ApiMessageLevel.WARNING, msgParams, messageSource);
    }


}
