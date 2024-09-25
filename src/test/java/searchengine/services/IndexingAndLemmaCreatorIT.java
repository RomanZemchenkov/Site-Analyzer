package searchengine.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import searchengine.BaseTest;
import searchengine.dao.model.Site;


import static org.assertj.core.api.Assertions.assertThat;


@Sql(value = "classpath:sql/init.sql")
public class IndexingAndLemmaCreatorIT extends BaseTest{

    private final IndexingAndLemmaService service;
    private final EntityManager entityManager;
    private static final String SITE_NAME = "ItDeti.ru";
    private static final String MAIN_SITE_URL = "https://itdeti.ru";
    private static final String RANDOM_PAGE = "https://itdeti.ru/robotrack";

    @Autowired
    public IndexingAndLemmaCreatorIT(IndexingAndLemmaService service, EntityManager entityManager) {
        this.service = service;
        this.entityManager = entityManager;
    }


    @Test
    @DisplayName("Testing the indexing and create lemma")
    @Commit
    void startIndexingAndCreateLemma(){
        Assertions.assertDoesNotThrow(service::startIndexingAndCreateLemma);

        Site savedSite = entityManager.createQuery("SELECT s FROM Site s WHERE s.name = :name", Site.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();

        Long countOfPages = entityManager.createQuery("SELECT count(p) FROM Page p WHERE p.site.name = :name", Long.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();
        Long countOfLemmas = entityManager.createQuery("SELECT count(l) FROM Lemma l WHERE l.site.name = :name", Long.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();
        Long countOfIndexes = entityManager.createQuery("SELECT count(i) FROM Index i WHERE i.lemma.site.name = :name", Long.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();

        assertThat(savedSite.getUrl()).isEqualTo(MAIN_SITE_URL);
        assertThat(countOfPages).isEqualTo(25);
        assertThat(countOfLemmas).isNotNull();
        assertThat(countOfIndexes).isNotNull();
    }

    @Test
    @DisplayName("Testing the indexing, lemma creating and index creating for one page")
    void startIndexingAndCreateLemmaForOnePage(){
        Assertions.assertDoesNotThrow(() -> service.startIndexingAndCreateLemmaForOnePage(RANDOM_PAGE));

        Long countOfLemmas = entityManager.createQuery("SELECT count(l) FROM Lemma l WHERE l.site.name = :name", Long.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();
        Long countOfIndexes = entityManager.createQuery("SELECT count(i) FROM Index i WHERE i.lemma.site.name = :name", Long.class)
                .setParameter("name", SITE_NAME)
                .getSingleResult();

        assertThat(countOfLemmas).isEqualTo(427);
        assertThat(countOfIndexes).isEqualTo(427);
    }
}
