package searchengine.services.searcher.analyzer;

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
import java.util.regex.Pattern;


public class PageAnalyzerImpl implements PageAnalyzer{

    private static final Pattern INVALID_EXTENSIONS = Pattern.compile("\\.(sql|zip|pdf|jpg|png|jpeg)$", Pattern.CASE_INSENSITIVE);

    public PageAnalyzerImpl(){}

    @Override
    public HttpResponseEntity searchLink(String url) {
        return searchLink(url,url);
    }

    public HttpResponseEntity searchLink(String url, String mainUrl) {
        HttpResponse connect;
        try{
            connect = createConnect(url);
        } catch (HttpStatusException statusException){
            return statusAnalyzed(statusException.getStatusCode(), statusException.getMessage(), url);
        }
        return createResponse(connect,url, mainUrl);
    }

    @Override
    public String searchPageTitle(String page) {
        return Jsoup.parse(page).title();
    }

    private HttpResponse createConnect(String url) throws HttpStatusException {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                               "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 YaBrowser/24.4.0.0 Safari/537.36")
                    .execute();

            int statusCode = response.statusCode();
            Document document = response.parse();
            return new HttpResponse(statusCode, document);
        } catch (HttpStatusException statusException) {
            throw statusException;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponseEntity createResponse(HttpResponse response, String url, String mainUrl) {
        Integer statusCode = response.getStatusCode();
        Document document = response.getDocument();

        Set<String> allLinksFromPage = parseToUrl(document, mainUrl);
        String htmlText = document.toString();

        return new NormalResponse(statusCode, url, htmlText, allLinksFromPage);
    }

    private Set<String> parseToUrl(Document document, String mainUrl) {
        Elements elements = document.select("a");

        Set<String> links = new HashSet<>();

        for (Element el : elements) {
            String page = el.attr("abs:href");
            if (isValidUrl(page, mainUrl)) {
                page = page.endsWith("/") ? page.substring(0, page.length() - 1) : page;
                links.add(page);
            }
        }
        return links;
    }

    private HttpResponseEntity statusAnalyzed(int statusCode, String message, String url) {
        return new ErrorResponse(statusCode, url, message);
    }

    private boolean isValidUrl(String url, String mainUrl) {
        return !url.isEmpty()
               && !url.contains("#")
               && !INVALID_EXTENSIONS.matcher(url).find()
               && url.startsWith(mainUrl);
    }
}
