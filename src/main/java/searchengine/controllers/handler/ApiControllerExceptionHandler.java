package searchengine.controllers.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.services.exception.IllegalPageException;

@RestControllerAdvice(basePackages = "searchengine.controllers")
public class ApiControllerExceptionHandler {

    @ExceptionHandler(value = {IllegalPageException.class})
    public ResponseEntity<ErrorResponse> illegalPageExceptionHandle(IllegalPageException exception){
        ErrorResponse errorResponse = new ErrorResponse("false",exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
