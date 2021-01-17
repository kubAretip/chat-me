package pl.chatme.service.exception;

public class AlreadyExistsException extends BusinessLogicException {
    public AlreadyExistsException(String title, String message) {
        super(title, message);
    }
}
