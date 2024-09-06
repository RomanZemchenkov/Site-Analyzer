package searchengine.services.exception;

import static searchengine.services.exception.ExceptionMessage.ILLEGAL_PAGE_EXCEPTION;

public class IllegalPageException extends RuntimeException{

    public IllegalPageException(){
        super(ILLEGAL_PAGE_EXCEPTION);
    }
}
