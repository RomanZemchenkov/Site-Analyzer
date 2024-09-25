package searchengine.dao.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.site.SiteRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Transactional()
public class LemmaRepositoryTest extends BaseTest {

    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final EntityManager entityManager;

    @Autowired
    public LemmaRepositoryTest(LemmaRepository lemmaRepository, SiteRepository siteRepository, EntityManager entityManager) {
        this.lemmaRepository = lemmaRepository;
        this.siteRepository = siteRepository;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("Test batch save for lemma")
    void batchSaveLemma(){
        Site site = siteRepository.findSiteByName("Sendel.ru").get();
        List<Lemma> lemmaList = new ArrayList<>();
        for(int i = 0; i < 1001; i++){
            Lemma lemma = new Lemma("lemma" + i, site);
            lemmaList.add(lemma);
        }
        time(() ->assertDoesNotThrow(() -> lemmaRepository.batchSave(lemmaList)));


        Long result = entityManager.createQuery("SELECT count(l) FROM Lemma AS l", Long.class).getSingleResult();

        assertThat(result).isEqualTo(1002);


    }

    static void time(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Метод отработал за: " + (finish - start));
    }
}
