package pl.chatme.exception;

import lombok.Getter;
import lombok.Setter;

public abstract class BusinessLogicException extends RuntimeException {

    @Getter
    @Setter
    private String title;

    public BusinessLogicException(String title, String message) {
        super(message);
        this.title = title;
    }

}
