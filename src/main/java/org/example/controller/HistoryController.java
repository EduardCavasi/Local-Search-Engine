package org.example.controller;

import org.example.service.file_search.history.SearchRequestHistoryService;
import org.example.service.file_search.history.SearchResultsHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "http://localhost:1420")
public class HistoryController {
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    private final SearchRequestHistoryService searchRequestHistoryService;
    private final SearchResultsHistoryService searchResultsHistoryService;

    public HistoryController(SearchRequestHistoryService searchRequestHistoryService, SearchResultsHistoryService searchResultsHistoryService) {
        this.searchRequestHistoryService = searchRequestHistoryService;
        this.searchResultsHistoryService = searchResultsHistoryService;
    }

    @GetMapping("/requests/top")
    public Map<String, Long> getTopRequests(@RequestParam int top){
        return searchRequestHistoryService.getTopSearchHistory(top);
    }

    @DeleteMapping("/requests")
    public void deleteRequestHistory(){
        searchRequestHistoryService.deleteAllSearchHistory();
        logger.info("Deleted request history");
    }

    @GetMapping("/requests/suggestions")
    public List<String> getSearchSuggestions(@RequestParam int top, @RequestParam String query){
        return searchRequestHistoryService.getTopSearchSuggestions(top, query);
    }

    @GetMapping("/results/top")
    public List<String> getTopResults(@RequestParam int top){
        return searchResultsHistoryService.getTopSearchHistory(top);
    }
    @DeleteMapping("/results")
    public void deleteResultHistory(){
        searchResultsHistoryService.deleteAllSearchHistory();
        logger.info("Deleted result history");
    }

}
