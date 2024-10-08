package searchengine.services;

import java.util.concurrent.atomic.AtomicLong;

public class GlobalVariables {

    public static final String STOP_INDEXING_TEXT = "Индексация остановлена пользователем.";
    public static final int COUNT_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public volatile static boolean INDEXING_STARTED = false;
    public volatile static boolean LEMMA_CREATING_STARTED = false;
    public volatile static boolean INDEX_CREATING_STARTED = false;
    public static final AtomicLong COUNT_OF_LEMMAS = new AtomicLong();

}
