package searchengine.services;

import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalVariables {

    public static final String STOP_INDEXING_TEXT = "The task was stopped by the user.";
    public static final int COUNT_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public volatile static boolean INDEXING_STARTED = false;
    public volatile static boolean LEMMA_CREATING_STARTED = false;
    public volatile static boolean INDEX_CREATING_STARTED = false;
    public static final ConcurrentHashMap<Page, HashMap<Lemma, Integer>> PAGE_AND_LEMMAS_WITH_COUNT = new ConcurrentHashMap<>();
    public static final AtomicLong COUNT_OF_LEMMAS = new AtomicLong();

}
