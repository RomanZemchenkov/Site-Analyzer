package searchengine.dao.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@Transactional
public class SiteRepositoryTest extends BaseTest {


    private final SiteRepository siteRepository;

    @Autowired
    public SiteRepositoryTest(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @ParameterizedTest
    @DisplayName("Test find all sites by name")
    @MethodSource("argumentsForFindAllByNameTest")
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
}
