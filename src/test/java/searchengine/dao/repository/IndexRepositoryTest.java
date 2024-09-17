package searchengine.dao.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;
import searchengine.dao.model.*;
import searchengine.dao.repository.index.IndexRepository;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.page.PageRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IndexRepositoryTest extends BaseTest {

    private final IndexRepository repository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final LemmaRepository lemmaRepository;

    @Autowired
    public IndexRepositoryTest(IndexRepository repository, PageRepository pageRepository, EntityManager entityManager, LemmaRepository lemmaRepository) {
        this.repository = repository;
        this.pageRepository = pageRepository;
        this.entityManager = entityManager;
        this.lemmaRepository = lemmaRepository;
    }

    @Test
    @DisplayName("Test for batch save method")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void batchSaveTest(){
        Page page = pageRepository.findAll().get(0);
        Lemma lemma = lemmaRepository.findAll().get(0);
        List<Index> indexList = new ArrayList<>();
        for(int i = 0; i < 1001; i++){
            Index index = new Index(page, lemma, (float) i + 1);
            indexList.add(index);
        }
        assertDoesNotThrow(() -> repository.createBatch(indexList));


        List<Index> selectIFromIndexI = entityManager.createQuery("SELECT i FROM Index i", Index.class).getResultList();

        assertThat(selectIFromIndexI).hasSize(1001);
    }
}
