package searchengine.dao.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;


@Transactional
public class SiteRepositoryTest extends BaseTest {

    private final SiteRepository repository;
    private static final String EXIST_SITE_NAME = "Test Site";
    private static final Integer EXIST_SITE_ID = 1;

    @Autowired
    public SiteRepositoryTest(SiteRepository repository) {
        this.repository = repository;
    }
}
