package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.searcher.lemma.LemmaWriter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final IndexingService indexingService;
    private final SiteRepository siteRepository;

    public void startIndexingAndCreateLemma(){
        indexingService.startIndexing();

        List<Site> sites = siteRepository.findAll();
        for(Site site : sites){
            List<Page> pages = site.getPages();
            for(Page page : pages){
            }
        }
    }
}
