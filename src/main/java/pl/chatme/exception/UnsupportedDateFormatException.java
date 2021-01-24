package pl.chatme.exception;

public class UnsupportedDateFormatException extends BusinessLogicException{
    public UnsupportedDateFormatException(String title, String message) {
        super(title, message);
    }
}
