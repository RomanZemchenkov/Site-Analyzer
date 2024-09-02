package searchengine.services.searcher;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.services.searcher.entity.ErrorResponse;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class PageAnalyzer {

    private final String mainUrl;

    public PageAnalyzer(String mainUrl) {
        this.mainUrl = mainUrl;
    }

    public HttpResponseEntity searchLink(String url) {
        return createConnect(url);
    }

    private HttpResponseEntity createConnect(String url){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                               "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 YaBrowser/24.4.0.0 Safari/537.36")
                    .execute();

            int statusCode = response.statusCode();
            Document document = response.parse();
            HttpResponse httpResponse = new HttpResponse(statusCode, document);

            return createResponse(httpResponse, url);
        } catch (HttpStatusException statusException){
            return statusAnalyzed(statusException.getStatusCode(),statusException.getMessage(), url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponseEntity createResponse(HttpResponse response, String url) {
        Integer statusCode = response.getStatusCode();
        Document document = response.getDocument();

        Set<String> allLinksFromPage = parseToUrl(document);
        String htmlText = document.toString();

        return new NormalResponse(statusCode, url, htmlText, allLinksFromPage);
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

    private HttpResponseEntity statusAnalyzed(int statusCode, String message, String url) {
        return new ErrorResponse(statusCode,url, message);
    }

    private boolean isValidUrl(String url) {
        return !url.isEmpty()
               && !url.contains("#")
               && !url.contains(".sql")
               && !url.contains(".zip")
               && !url.contains(".pdf")
               && !url.contains(".jpg")
               && url.startsWith(mainUrl);
    }
}
