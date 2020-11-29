package ycastor.me.miro.api.advisors;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ycastor.me.miro.shared.Problem;
import ycastor.me.miro.widgets.exceptions.WidgetNotFoundException;

@ControllerAdvice
public class ErrorAdvisor {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public final ResponseEntity<Problem> validationErrors(MethodArgumentNotValidException validationException) {
        var error = new Problem(
                "Invalid Request", validationException.getAllErrors()
                                                      .stream()
                                                      .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                                      .collect(Collectors.joining()));
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({WidgetNotFoundException.class})
    public final ResponseEntity<Problem> widgetNotFound(WidgetNotFoundException widgetNotFoundException) {
        var error = new Problem("Widget not found", widgetNotFoundException.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
