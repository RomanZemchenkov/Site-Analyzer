package searchengine.services.searcher.analyzer;

import searchengine.services.dto.page.FindPageDto;

public interface Indexing {

    void startSitesIndexing();

    FindPageDto startPageIndexing(String searchedUrl);

    void stopIndexing();
}
