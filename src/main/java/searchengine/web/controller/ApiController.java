package searchengine.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.searcher.analyzer.SiteIndexingImpl;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.service.SearchService;
import searchengine.services.service.StatisticsService;
import searchengine.web.entity.ErrorResponse;
import searchengine.web.entity.NormalResponse;
import searchengine.web.entity.Response;
import searchengine.web.entity.SearchResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static searchengine.services.GlobalVariables.INDEXING_STARTED;
import static searchengine.services.exception.ExceptionMessage.INDEXING_ALREADY_START;
import static searchengine.services.exception.ExceptionMessage.INDEXING_DOESNT_START;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteIndexingImpl indexingService;
    private final IndexingService indexingAndLemmaService;
    private final SearchService searchService;


    @GetMapping("/statistics")
    @CheckTimeWorking
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getTotalStatistic());
    }

    @GetMapping("/startIndexing")
    @CheckTimeWorking
    public ResponseEntity<Response> startIndexing() {
        if (!INDEXING_STARTED.get()) {
            searchService.clearPrevInformation();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(indexingAndLemmaService::startIndexingAndCreateLemma);
            executorService.shutdown();

            return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("false", INDEXING_ALREADY_START), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/stopIndexing")
    @CheckTimeWorking
    public ResponseEntity<Response> stopIndexing() {
        if (INDEXING_STARTED.get()) {
            indexingService.stopIndexing();
            return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("false", INDEXING_DOESNT_START), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/indexPage")
    @CheckTimeWorking
    public ResponseEntity<Response> indexPage(@RequestParam String url) {
        searchService.clearPrevInformation();
        indexingAndLemmaService.startIndexingAndCreateLemmaForOnePage(url);
        return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
    }

    @GetMapping("/search")
    @CheckTimeWorking
    public ResponseEntity<Response> search(@RequestParam(name = "query") String query,
                                           @RequestParam(name = "limit", required = false, defaultValue = "10") String limit,
                                           @RequestParam(name = "offset", required = false, defaultValue = "0") String offset,
                                           @RequestParam(name = "site", required = false) String siteUrl) {



        SearchParametersDto searchParametersDto = new SearchParametersDto(query, limit, offset, siteUrl);
        SearchResponse searchResponse = searchService.search(searchParametersDto);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }
}
