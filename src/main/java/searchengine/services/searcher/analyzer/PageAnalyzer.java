package searchengine.services.searcher.analyzer;

import searchengine.services.searcher.entity.HttpResponseEntity;

public interface PageAnalyzer {

    HttpResponseEntity searchLink(String url, String mainUrl);

    HttpResponseEntity searchLink(String url);

    String searchPageTitle(String page);

}
