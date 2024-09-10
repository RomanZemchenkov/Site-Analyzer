package searchengine.services.searcher;

import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVariables {

    public static final Set<Integer> errorStatusCodes = Set.of(400, 401, 402, 403, 404, 405, 500, 501, 502, 503);
    public static final String STOP_INDEXING_TEXT = "The task was stopped by the user.";
    public volatile static boolean INDEXING_STARTED = false;
    public static final ConcurrentHashMap<Page, HashMap<Lemma, Integer>> pageAndLemmasWithCount = new ConcurrentHashMap<>();

}
