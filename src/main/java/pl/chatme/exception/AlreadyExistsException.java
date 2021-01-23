package pl.chatme.exception;

public class AlreadyExistsException extends BusinessLogicException {
    public AlreadyExistsException(String title, String message) {
        super(title, message);
    }
}
