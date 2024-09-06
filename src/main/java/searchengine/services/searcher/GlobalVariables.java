package searchengine.services.searcher;

import java.util.Set;

public class GlobalVariables {

    public static final Set<Integer> errorStatusCodes = Set.of(400, 401, 402, 403, 404, 405, 500, 501, 502, 503);
    public static final String STOP_INDEXING_TEXT = "The task was stopped by the user.";
    public volatile static boolean INDEXING_STARTED = false;

}
