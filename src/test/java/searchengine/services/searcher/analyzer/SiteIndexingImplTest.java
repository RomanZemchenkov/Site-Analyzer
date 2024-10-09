package searchengine.services.searcher.analyzer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.exception.IllegalPageException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static searchengine.services.GlobalVariables.STOP_INDEXING_TEXT;


public class SiteIndexingImplTest extends BaseTest {

    private final SiteIndexingImpl service;
    private final EntityManager manager;
    private static final String ALWAYS_GOOD_SITE = "ItDeti.ru";
    private static final String EXCEPTION_SITE = "ItDetiWithException.ru";

    @Autowired
    public SiteIndexingImplTest(SiteIndexingImpl service, EntityManager manager) {
        this.service = service;
        this.manager = manager;
    }

    @Test
    @DisplayName("Testing the indexing one site without problems")
    void startSitesIndexingWithoutProblemTest() {
        Assertions.assertDoesNotThrow(service::startSitesIndexing);

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 4", Site.class)
                .getResultList();

        sites.forEach(er -> {
            assertThat(er.getLastError()).isNull();
            assertThat(er.getPages()).isNotNull();
            assertThat(er.getStatus()).isEqualTo(Status.INDEXED);
        });
    }

    @Test
    @DisplayName("Testing the indexing site with problem")
    void startSitesIndexingWithProblemTest() {
        Assertions.assertDoesNotThrow(service::startSitesIndexing);

        Site site = manager.createQuery("SELECT s FROM Site AS s WHERE s.id = 4", Site.class)
                .getSingleResult();


        assertThat(site.getPages()).isNotNull();
        assertThat(site.getLastError()).isNotBlank();
        assertThat(site.getStatus()).isEqualTo(Status.FAILED);
    }

    @Test
    @DisplayName("Testing the indexing sites where one site with problem")
    void startSitesIndexingNormalAndProblemSitesTest() {
        Assertions.assertDoesNotThrow(service::startSitesIndexing);


        Tuple tupleWithException = manager.createQuery("SELECT s.status AS status, s.lastError AS error ,count(p.path) AS page_count FROM Site AS s " +
                                                       "LEFT JOIN Page AS p ON p.site.id = s.id " +
                                                       "WHERE s.name = :name " +
                                                       "GROUP BY s.status, s.lastError", Tuple.class)
                .setParameter("name", EXCEPTION_SITE).getSingleResult();
        Status status = tupleWithException.get(0, Status.class);
        String error = tupleWithException.get(1, String.class);
        Long countOfPages = tupleWithException.get(2, Long.class);

        Site siteWithoutException = manager.createQuery("SELECT s FROM Site AS s" +
                                                        " join Page AS p ON p.site.id = s.id WHERE s.name = :name", Site.class)
                .setParameter("name", ALWAYS_GOOD_SITE).getSingleResult();

        assertThat(status).isEqualTo(Status.FAILED);
        assertThat(error).isNotBlank();
        assertThat(countOfPages).isEqualTo(0);

        assertThat(siteWithoutException.getLastError()).isNull();
        assertThat(siteWithoutException.getStatus()).isEqualTo(Status.INDEXED);
    }

    @Test
    @DisplayName("Testing the indexing with stop method use")
    void startIndexingWithStopSiteIndexingTest() {
        Assertions.assertDoesNotThrow(() -> {
            Thread threadStart = new Thread(service::startSitesIndexing);
            Thread threadStop = new Thread(service::stopIndexing);

            threadStart.start();
            Thread.sleep(1000L);

            threadStop.start();

            threadStart.join();
            threadStop.join();
        });

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 4", Site.class)
                .getResultList();

        sites.forEach(er -> {
            assertThat(er.getStatus()).isEqualTo(Status.FAILED);
            assertThat(er.getLastError()).isEqualTo(STOP_INDEXING_TEXT);
            assertThat(er.getPages()).isNotNull();
        });

    }

    @Test
    @DisplayName("Successful testing the one page indexing")
    void successfulIndexingOnePageTest() {
        String searchedUrl = "https://itdeti.ru/robotrack";
        FindPageDto infoDto = service.startPageIndexing(searchedUrl);
        Page savedPage = infoDto.getSavedPage();
        Site site = infoDto.getSite();

        assertThat(savedPage.getCode()).isEqualTo(200);
        assertThat(savedPage.getSite().getUrl()).isEqualTo("https://itdeti.ru");
        assertThat(savedPage.getPath()).isNotBlank();
        assertThat(site.getStatus()).isEqualTo(Status.INDEXED);
    }

    @Test
    @DisplayName("Successful testing the one page indexing for several pages")
    void successfulIndexingSeveralPage() {
        String searchedUrl1 = "https://itdeti.ru/robotrack";
        String searchedUrl2 = "https://itdeti.ru/minecraft";
        String searchedUrl3 = "https://itdeti.ru/systems-admin";
        service.startPageIndexing(searchedUrl1);
        service.startPageIndexing(searchedUrl2);
        service.startPageIndexing(searchedUrl3);

        Long countOfPages = manager.createQuery("SELECT count(*) FROM Page p WHERE p.site.id = :siteId", Long.class)
                .setParameter("siteId", "4")
                .getSingleResult();

        assertThat(countOfPages).isEqualTo(3L);
    }

    @Test
    @DisplayName("Unsuccessful testing the one page indexing")
    void unsuccessfulIndexingOnePageTest() {
        String searchedUrl = "https://ru.wikipedia.org/wiki/Заглавная_страница";
        Assertions.assertThrows(IllegalPageException.class, () -> service.startPageIndexing(searchedUrl));
    }


}
