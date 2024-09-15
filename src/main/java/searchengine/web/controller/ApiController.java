package searchengine.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.searcher.analyzer.Indexing;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingAndLemmaService;
import searchengine.services.service.StatisticsService;
import searchengine.web.Response;

import static searchengine.services.GlobalVariables.INDEXING_STARTED;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final Indexing indexingService;
    private final IndexingAndLemmaService indexingAndLemmaService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getTotalStatistic());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing(){
        if(!INDEXING_STARTED){
            indexingAndLemmaService.startIndexingAndCreateLemma();
            return new ResponseEntity<>(new Response("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Response("false","Индексация уже запущена."),HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing(){
        if(INDEXING_STARTED){
            indexingService.stopIndexing();
            return new ResponseEntity<>(new Response("true"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Response("false", "Индексация не запущена."), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestBody FindPageDto dto){
        indexingAndLemmaService.startIndexingAndCreateLemmaForOnePage(dto);
        return new ResponseEntity<>(new Response("true"),HttpStatus.OK);
    }
}
