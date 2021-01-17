package pl.chatme.service.exception;

public class InvalidDataException extends BusinessLogicException {
    public InvalidDataException(String title, String message) {
        super(title, message);
    }
}
