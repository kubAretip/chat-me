package pl.chatme.service.error;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException() {
        super("Email is already in use!");
    }
}
