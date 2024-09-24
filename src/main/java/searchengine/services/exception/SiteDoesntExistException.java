package searchengine.services.exception;

import static searchengine.services.exception.ExceptionMessage.SITE_DOESNT_EXIST_EXCEPTION;

public class SiteDoesntExistException extends RuntimeException{

    public SiteDoesntExistException(){
        super(SITE_DOESNT_EXIST_EXCEPTION);
    }
}
