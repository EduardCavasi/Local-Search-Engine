package org.example.controller;

import org.example.error.SearchRequestParseException;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_search.SearchEngine;
import org.example.service.file_search.SearchRequestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:1420")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final SearchRequestParser searchRequestParser;
    private final SearchEngine searchEngine;

    public SearchController(SearchRequestParser searchRequestParser, SearchEngine searchEngine) {
        this.searchRequestParser = searchRequestParser;
        this.searchEngine = searchEngine;
    }

    /**endpoint for searching by receiving a SearchParams object*/
    @PostMapping
    public ResponseEntity<?> executeSearchQuery(@RequestBody String searchRequest){
        logger.info("Parsing search query: {}", searchRequest);
        try {
            SearchParams searchParams = searchRequestParser.parse(searchRequest);
            Optional<List<FilePreview>> previews = searchEngine.executeQuery(searchParams);
            List<FilePreview> ans = new ArrayList<>();
            if(previews.isPresent()){
                ans = previews.get();
            }
            logger.info("Finished search query : {}, returning {} results", searchParams, ans.size());
            return ResponseEntity.ok(ans);
        }
        catch (SearchRequestParseException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/ranking_algorithm")
    public void modifyRankingAlgorithm(@RequestParam String type){
        searchEngine.modifyRankingAlgorithm(type);
    }
}