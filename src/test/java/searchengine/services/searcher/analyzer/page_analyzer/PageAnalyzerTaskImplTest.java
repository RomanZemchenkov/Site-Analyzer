package searchengine.services.searcher.analyzer.page_analyzer;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.service.SiteService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@SpringBootTest
public class PageAnalyzerTaskImplTest extends BaseTest {

    private final PageAnalyzerTaskFactory factory;
    private final SiteService siteService;
    private static final String TEST_PAGE_URL = "https://itdeti.ru/robotrack";
    private static final String TEST_MAIN_URL = "https://itdeti.ru";
    private final EntityManager entityManager;

    @Autowired
    public PageAnalyzerTaskImplTest(PageAnalyzerTaskFactory factory, SiteService siteService, EntityManager entityManager) {
        this.factory = factory;
        this.siteService = siteService;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("Testing the page analyzer task")
    void pageAnalyzerTaskTest() {
        ShowSiteDto siteDto = siteService.createSite(new CreateSiteDto(TEST_MAIN_URL, "ItDeti"));
        PageParseContext pageContext = new PageParseContext(siteDto);
        PageAnalyzerTask task = factory.createTask(TEST_PAGE_URL, pageContext);
        assertDoesNotThrow(task::analyze);

        Long result = entityManager.createQuery("SELECT count(p) FROM Page p WHERE p.site.id = :siteId", Long.class)
                .setParameter("siteId", siteDto.getId())
                .getSingleResult();

        Assertions.assertThat(result).isEqualTo(1);

    }
}
