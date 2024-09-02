package searchengine.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import searchengine.BaseTest;
import searchengine.services.searcher.ParseContext;
import searchengine.services.searcher.SiteAnalyzerTask;
import searchengine.services.searcher.SiteAnalyzerTaskFactory;

import java.util.concurrent.ForkJoinPool;

@SpringBootTest
public class ParseTaskTest extends BaseTest{

    private final SiteAnalyzerTaskFactory factory;

    @Autowired
    public ParseTaskTest(SiteAnalyzerTaskFactory factory) {
        this.factory = factory;
    }

     @Test
    void taskTest(){
        String url = "https://sendel.ru/";
        ParseContext context = new ParseContext("1","site",url, factory);
        SiteAnalyzerTask task = factory.createTask(url, context);
        ForkJoinPool pool = new ForkJoinPool(12);


        pool.invoke(task);

        System.out.println(" ");
    }

//    @Test
//    void taskTest2(){
//        ForkJoinPool pool = new ForkJoinPool(2);
//        String url = "https://ru-brightdata.com/";
//        ParseContext context = new ParseContext("1",url, new ConcurrentSkipListSet<>());
//        SiteAnalyzerTask parseTask = new SiteAnalyzerTask(url, context);
//
//        List<CreatePageDto> result = pool.invoke(parseTask);
//        System.out.println(result.size());
//        result.forEach(dto -> System.out.println(dto.getPath()));
//    }
}
