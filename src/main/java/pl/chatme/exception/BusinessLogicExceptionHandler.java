package pl.chatme.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BusinessLogicExceptionHandler {

    @ExceptionHandler(value = {AlreadyExistsException.class})
    public ResponseEntity<Problem> handleAlreadyExistsException(final AlreadyExistsException ex, final NativeWebRequest request) {
        var problem = Problem.builder()
                .withStatus(Status.CONFLICT)
                .withTitle(ex.getTitle())
                .withDetail(ex.getLocalizedMessage())
                .build();
        return new ResponseEntity<>(problem, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {InvalidDataException.class})
    public ResponseEntity<Problem> handleInvalidDataException(InvalidDataException ex) {
        var problem = Problem.builder()
                .withStatus(Status.BAD_REQUEST)
                .withTitle(ex.getTitle())
                .withDetail(ex.getLocalizedMessage())
                .build();
        return new ResponseEntity<>(problem, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Problem> handleNotFoundException(NotFoundException ex) {
        var problem = Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .withTitle(ex.getTitle())
                .withDetail(ex.getLocalizedMessage())
                .build();
        return new ResponseEntity<>(problem, HttpStatus.NOT_FOUND);
    }

}
