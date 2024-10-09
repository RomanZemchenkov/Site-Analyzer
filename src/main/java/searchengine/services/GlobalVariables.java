package searchengine.services;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalVariables {

    public static final String STOP_INDEXING_TEXT = "Индексация остановлена пользователем.";
    public static final int COUNT_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static AtomicBoolean INDEXING_STARTED = new AtomicBoolean(false);
    public static AtomicBoolean LEMMA_CREATING_STARTED = new AtomicBoolean(false);
    public static AtomicBoolean INDEX_CREATING_STARTED = new AtomicBoolean(false);
    public static final AtomicLong COUNT_OF_LEMMAS = new AtomicLong();

}
