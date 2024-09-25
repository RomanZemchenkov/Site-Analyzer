package searchengine.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.dto.page.ShowPageDto;
import searchengine.services.exception.IllegalPageException;
import searchengine.services.searcher.analyzer.Indexing;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingAndLemmaService;
import searchengine.services.service.SearchService;
import searchengine.services.service.StatisticsService;
import searchengine.web.handler.ErrorResponse;
import searchengine.web.handler.NormalResponse;
import searchengine.web.handler.Response;
import searchengine.web.entity.SearchResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static searchengine.services.GlobalVariables.INDEXING_STARTED;
import static searchengine.services.exception.ExceptionMessage.INDEXING_ALREADY_START;
import static searchengine.services.exception.ExceptionMessage.INDEXING_DOESNT_START;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final Indexing indexingService;
    private final IndexingAndLemmaService indexingAndLemmaService;
    private final SearchService searchService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getTotalStatistic());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
        if (!INDEXING_STARTED) {
            time(() -> indexingAndLemmaService.startIndexingAndCreateLemma());
            return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("false", INDEXING_ALREADY_START), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        if (INDEXING_STARTED) {
            indexingService.stopIndexing();
            return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse("false", INDEXING_DOESNT_START), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String url) {
        indexingAndLemmaService.startIndexingAndCreateLemmaForOnePage(url);
        return new ResponseEntity<>(new NormalResponse("true"), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "limit", required = false, defaultValue = "20") String limit,
                                                 @RequestParam(name = "offset", required = false, defaultValue = "0") String offset,
                                                 @RequestParam(name = "url", required = false) String url) {
        SearchParametersDto searchParametersDto = new SearchParametersDto(query, limit, offset, url);
        System.out.println("Поиск запущен");
        HashMap<Integer, List<ShowPageDto>> searchResult = searchService.search(searchParametersDto);
        int countOfPages = 0;
        for (Map.Entry<Integer, List<ShowPageDto>> entry : searchResult.entrySet()) {
            countOfPages += entry.getValue().size();
        }
        System.out.println("Поиск закончен");
        List<ShowPageDto> offsetList = searchResult.getOrDefault(Integer.parseInt(offset), searchResult.get(searchResult.size() - 1));
        SearchResponse searchResponse = new SearchResponse(true, countOfPages, offsetList);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }

    static void time(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Индексация отработала за: " + (finish - start));
    }
}
