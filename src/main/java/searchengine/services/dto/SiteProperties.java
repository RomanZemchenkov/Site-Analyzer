package searchengine.services.dto;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@ConfigurationProperties(prefix = "indexing-settings")
public class SiteProperties {

    private final List<Site> sites;

    public SiteProperties(List<Site> sites) {
        this.sites = sites;
    }


    @Getter
    public static class Site{

        private final String url;
        private final String name;

        public Site(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }
}
