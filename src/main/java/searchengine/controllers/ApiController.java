package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.IndexingService;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing(){
        indexingService.startIndexing();
        return new ResponseEntity<>(new Response("true"), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing(){
        indexingService.stopIndexing();
        return new ResponseEntity<>(new Response("true"), HttpStatus.OK);
    }
}
