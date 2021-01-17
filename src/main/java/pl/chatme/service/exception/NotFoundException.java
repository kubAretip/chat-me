package pl.chatme.service.exception;

public class NotFoundException extends BusinessLogicException {
    public NotFoundException(String title, String message) {
        super(title, message);
    }
}
