package searchengine.services.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Page;
import searchengine.services.dto.page.CreatePageDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PageServiceTest extends BaseTest {

    private final PageService service;
    private static final String EXIST_SITE_ID = "1";
    private static final String EXIST_URL = "/test/url";

    @Autowired
    public PageServiceTest(PageService service) {
        this.service = service;
    }

    @Test
    @DisplayName("Testing the create page with id")
    void pageWithIdSave(){
        CreatePageDto dto = new CreatePageDto(EXIST_SITE_ID, "/page/to", "200", "content");
        Page page = assertDoesNotThrow(() -> service.createPage(dto));

        assertThat(page.getPath()).isEqualTo("/page/to");
    }

}
