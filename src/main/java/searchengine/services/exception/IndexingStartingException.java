package searchengine.services.exception;

public class IndexingStartingException extends RuntimeException{

    public IndexingStartingException(){
        super(ExceptionMessage.INDEXING_STARTING_EXCEPTION);
    }
}
