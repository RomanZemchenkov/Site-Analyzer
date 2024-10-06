package searchengine.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.services.exception.EmptyQueryException;
import searchengine.services.exception.IllegalPageException;
import searchengine.services.exception.IndexingStartingException;
import searchengine.services.exception.SiteDoesntExistException;
import searchengine.web.entity.ErrorResponse;

@RestControllerAdvice(basePackages = "searchengine.web.controller")
public class ApiControllerExceptionHandler {

    @ExceptionHandler(value = {IllegalPageException.class})
    public ResponseEntity<ErrorResponse> illegalPageExceptionHandle(IllegalPageException exception){
        ErrorResponse errorResponse = new ErrorResponse("false",exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = {SiteDoesntExistException.class})
    public ResponseEntity<ErrorResponse> siteDoesntExistExceptionHandle(SiteDoesntExistException exception){
        ErrorResponse errorResponse = new ErrorResponse("false", exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EmptyQueryException.class})
    public ResponseEntity<ErrorResponse> emptyQueryExceptionHandle(EmptyQueryException exception){
        ErrorResponse errorResponse = new ErrorResponse("false", exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {IndexingStartingException.class})
    public ResponseEntity<ErrorResponse> indexingStartingExceptionHandle(IndexingStartingException exception){
        ErrorResponse errorResponse = new ErrorResponse("false", exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.METHOD_NOT_ALLOWED);
    }
}
