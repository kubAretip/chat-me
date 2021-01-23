package pl.chatme.exception;

public class InvalidDataException extends BusinessLogicException {
    public InvalidDataException(String title, String message) {
        super(title, message);
    }
}
