package searchengine.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SiteServiceTest extends BaseTest {

    private final SiteService service;
    private static final String EXIST_SITE_NAME = "Test Site";
    private static final String EXIST_SITE_ID = "1";
    private static final String EXIST_URL = "/test/url";

    @Autowired
    public SiteServiceTest(SiteService service) {
        this.service = service;
    }

    @Test
    @DisplayName("Тест создания сайта")
    void createSite(){
//        ShowSiteDto site = assertDoesNotThrow(() -> service.createSite(new CreateSiteDto("/url/test", "Site name")));
//
//        assertThat(site.getName()).isEqualTo(EXIST_SITE_NAME);
    }

    @Test
    @DisplayName("Тест удачного обновления сайта")
    void successfulUpdateSite(){
        UpdateSiteDto dto = new UpdateSiteDto(EXIST_SITE_ID, "INDEXING", "", EXIST_URL, EXIST_SITE_NAME);
        Boolean result = assertDoesNotThrow(() -> service.updateSite(dto));
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Тест неудачного обновления сайта")
    void unsuccessfulUpdateSite(){
        UpdateSiteDto dto = new UpdateSiteDto(EXIST_SITE_ID, "INDEXING", "Error",EXIST_URL , EXIST_SITE_NAME);
        Boolean result = assertDoesNotThrow(() -> service.updateSite(dto));
        assertThat(result).isFalse();
    }
}
