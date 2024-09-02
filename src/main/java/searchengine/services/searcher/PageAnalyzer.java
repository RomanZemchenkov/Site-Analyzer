package searchengine.services.searcher;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static searchengine.services.searcher.ConstantsCode.errorStatusCodes;

public class PageAnalyzer {

    private final String mainUrl;

    public PageAnalyzer(String mainUrl) {
        this.mainUrl = mainUrl;
    }

    public ResponseEntity searchLink(String url) {
        HttpResponse response = createConnect(url);

        Integer statusCode = response.getStatusCode();
        Document documentOfUrl = response.getDocument();

        return createResponse(documentOfUrl, statusCode, url);
    }


    private ResponseEntity createResponse(Document document, Integer statusCode, String url) {
        Set<String> allLinksFromPage = checkAvailableStatusCode(statusCode) ? parseToUrl(document) : Set.of();
        String htmlText = document.toString();

        return new ResponseEntity(statusCode, url, htmlText, allLinksFromPage);
    }

    private Set<String> parseToUrl(Document document) {
        Elements elements = document.select("a");

        Set<String> links = new HashSet<>();

        for (Element el : elements) {
            String page = el.attr("abs:href");
            if (isValidUrl(page)) {
                links.add(page);
            }
        }
        return links;
    }

    private HttpResponse createConnect(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                               "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 YaBrowser/24.4.0.0 Safari/537.36")
                    .execute();

            int statusCode = getStatusCode(response);
            Document document = response.parse();
            return new HttpResponse(statusCode,document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkAvailableStatusCode(Integer statusCode){
        return !errorStatusCodes.contains(statusCode);
    }

    private int getStatusCode(Connection.Response response) {
        return response.statusCode();
    }

    private boolean isValidUrl(String url) {
        return !url.isEmpty()
               && !url.contains("#")
               && !url.contains(".sql")
               && !url.contains(".zip")
               && !url.contains(".pdf")
               && url.startsWith(mainUrl);
    }
}
