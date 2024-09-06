package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.LemmaRepository;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.parser.TextToLemmaParser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private final LemmaRepository lemmaRepository;
    private final TextToLemmaParser parser = new TextToLemmaParser();
    private final RedisRepository redis;

    public void createLemma(Page page, Site site){
        String htmlContent = page.getContent();
        HashMap<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        checkLemmas(mapOfLemmas,site);

    }

    private void checkLemmas(HashMap<String, Integer> mapOfLemmas, Site site){
        String siteName = site.getName();
        for(Map.Entry<String,Integer> entry : mapOfLemmas.entrySet()){
            String lemma = entry.getKey();
            Optional<Lemma> mayBeExistLemma = redis.getLemma(siteName, lemma);
            mayBeExistLemma.ifPresentOrElse(lem -> ifLemmaExist(lem,site),() -> ifNewLemma(lemma,site));
        }
    }

    private void ifNewLemma(String lemma,Site site){
        Lemma newLemma = new Lemma(lemma, site);
        Lemma savedLemma = lemmaRepository.saveAndFlush(newLemma);
        redis.saveUseLemma(site.getName(),savedLemma);
    }

    private void ifLemmaExist(Lemma lemma, Site site){
        Integer currentFrequency = lemma.getFrequency();
        lemma.setFrequency(currentFrequency + 1);
        redis.saveUseLemma(site.getName(),lemma);
    }
}
