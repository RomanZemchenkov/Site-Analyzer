package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.parser.TextToLemmaParser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private final TextToLemmaParser parser = new TextToLemmaParser();
    private final RedisRepository redis;

    public void createLemma(Page page, Site site){
        TextToLemmaParser parser = new TextToLemmaParser();
        String htmlContent = page.getContent();
        HashMap<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        checkLemmas(mapOfLemmas,site);
        System.out.println("Лемма создана");
    }

    public List<Lemma> getAllLemmasOnSite(Site site){
        return redis.getAllLemmasOnSite(site.getName());
    }

    private void checkLemmas(HashMap<String,Integer> mapOfLemmas, Site site){
        String siteName = site.getName();
        for(Map.Entry<String,Integer> entry : mapOfLemmas.entrySet()){
            String lemma = entry.getKey();
            Lemma saveLemma = new Lemma(lemma, site);
            redis.saveUseLemma(siteName,saveLemma);
        }
    }
            /*
            Добавить разбиение по буквам для ускорения поиска
             */
//    private void checkLemmas(HashMap<String, Integer> mapOfLemmas, Site site){
//        String siteName = site.getName();
//        for(Map.Entry<String,Integer> entry : mapOfLemmas.entrySet()){
//            String lemma = entry.getKey();
//            Optional<Lemma> mayBeExistLemma = redis.getLemma(siteName, lemma);
//            mayBeExistLemma.ifPresentOrElse(lem -> ifLemmaExist(lem,site),() -> ifNewLemma(lemma,site));
//        }
//    }

//    private void ifNewLemma(String lemma,Site site){
//        Lemma newLemma = new Lemma(lemma, site);
//        Lemma savedLemma = lemmaRepository.saveAndFlush(newLemma);
//        redis.saveUseLemma(site.getName(),savedLemma);
//    }
//
//    private void ifLemmaExist(Lemma lemma, Site site){
//        Integer currentFrequency = lemma.getFrequency();
//        lemma.setFrequency(currentFrequency + 1);
//        System.out.println(currentFrequency);
//        redis.saveUseLemma(site.getName(),lemma);
//    }
}
