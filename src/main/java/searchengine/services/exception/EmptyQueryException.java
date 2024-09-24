package searchengine.services.exception;

public class EmptyQueryException extends RuntimeException{

    public EmptyQueryException(){
        super(ExceptionMessage.EMPTY_QUERY_EXCEPTION);
    }
}
