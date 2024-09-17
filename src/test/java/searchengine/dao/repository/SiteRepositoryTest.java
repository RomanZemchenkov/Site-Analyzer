package searchengine.dao.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.IndexingAndLemmaService;
import searchengine.services.service.SiteService;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class SiteRepositoryTest extends BaseTest {


    private final SiteRepository siteRepository;
    private final IndexingAndLemmaService indexingAndLemmaService;

    @Autowired
    public SiteRepositoryTest(SiteRepository siteRepository,IndexingAndLemmaService indexingAndLemmaService) {
        this.siteRepository = siteRepository;
        this.indexingAndLemmaService = indexingAndLemmaService;
    }

    @ParameterizedTest
    @DisplayName("Test find all sites by name")
    @MethodSource("argumentsForFindAllByNameTest")
    @Transactional
    void findAllByName(Set<String> sitesName, int expectedCountOfSite){
        List<Site> sites = assertDoesNotThrow(() -> siteRepository.findAllByName(sitesName));

        assertThat(sites).hasSize(expectedCountOfSite);
    }

    static Stream<Arguments> argumentsForFindAllByNameTest(){
        return Stream.of(
                Arguments.of(Set.of("Sendel.ru"),1),
                Arguments.of(Set.of("Sendel.ru","Test Site"),2),
                Arguments.of(Set.of("ItDeti.ru"),0)
        );
    }

    @Disabled
    @Test
    @DisplayName("Testing the delete all information by site")
    @Commit
    void deleteAllInformation(){
        indexingAndLemmaService.startIndexingAndCreateLemma();
        Site site = siteRepository.findSiteByName("BalakhnaSchool11").get();
        System.out.println("Процесс удаления запущен");
        time(() -> siteRepository.deleteAllInfoBySite(site));
        System.out.println(" ");
    }

    //23 секунды через entityManager 1 вариант
    //21.8 через entityManager без каскадного удаления в таблицах
    //19.6 с созданием индекса без каскадного удаления
    //8.4 с батчем
    //1 со снятием триггеров
    static void time(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Метод отработал за: " + (finish - start));
    }

}
