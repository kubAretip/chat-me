package pl.chatme.util;

import org.springframework.stereotype.Component;
import pl.chatme.exception.NotFoundException;
import pl.chatme.exception.UnsupportedDateFormatException;

@Component
public class ExceptionUtils {

    private final Translator translator;

    public ExceptionUtils(Translator translator) {
        this.translator = translator;
    }

    public NotFoundException userNotFoundException(String login) {
        return new NotFoundException(translator.translate("exception.user.not.found"),
                translator.translate("exception.user.not.found.body", new Object[]{login}));
    }

    public NotFoundException conversationNotFoundException() {
        return new NotFoundException(translator.translate("exception.conversation.not.found"),
                translator.translate("exception.conversation.not.found.body"));
    }

    public UnsupportedDateFormatException unsupportedDateFormatException() {
        return new UnsupportedDateFormatException(translator.translate("exception.unsupported.date"),
                translator.translate("exception.unsupported.date.body", new Object[]{DateUtils.DATE_PATTERN}));
    }


}
