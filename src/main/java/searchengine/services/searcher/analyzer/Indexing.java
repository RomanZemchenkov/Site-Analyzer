package searchengine.services.searcher.analyzer;

import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.services.dto.page.FindPageDto;

import java.util.HashMap;
import java.util.List;

public interface Indexing {

//    void startSitesIndexing();

    HashMap<Site, List<Page>> startSitesIndexing();

    FindPageDto startPageIndexing(String searchedUrl);

    void stopIndexing();
}
