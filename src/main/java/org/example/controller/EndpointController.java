package org.example.controller;

import org.example.error.SearchRequestParseException;
import org.example.model.general.EngineRules;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_save.IndexingService;
import org.example.service.file_save.IndexingStats;
import org.example.service.file_search.SearchEngine;
import org.example.service.file_search.SearchRequestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**Controller class
 * Creates endpoint to access backend functionality
 * Cross Origin permitted fo frontend origin
 */
@RestController
@CrossOrigin(origins = "http://localhost:1420")
public class EndpointController {
    private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);
    private final SearchEngine searchEngine;
    private final IndexingService indexingService;
    private final EngineRules engineRules;
    private final IndexingStats indexingStats;
    private final SearchRequestParser searchRequestParser;
    public EndpointController(IndexingService indexingService, SearchEngine searchEngine, EngineRules engineRules, IndexingStats indexingStats, SearchRequestParser searchRequestParser) {
        this.searchEngine = searchEngine;
        this.indexingService = indexingService;
        this.engineRules = engineRules;
        this.indexingStats = indexingStats;
        this.searchRequestParser = new SearchRequestParser();
    }

    /**endpoint for starting indexing*/
    @PostMapping("/index")
    public void startIndexing(){
        indexingService.storeFileSystemSnapshot();
    }

    /**endpoint for searching by receiving a SearchParams object*/
    @PostMapping("/search")
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

    /**endpoint for modifying the ignore extensions engine rules
     * type = 0 for reset
     * type = 1 for adding extension
     * type = 2 for deleting extension
     */
    @PostMapping("/post_ignore_extension_rules")
    public void manageIgnoreExtensionRules(@RequestParam String extension, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnoreExtensions();
            case 1 -> engineRules.addIgnoreExtensions(extension);
            case 2 -> engineRules.deleteIgnoreExtensions(extension);
        }
    }

    /**endpoint for modifying the ignore directory engine rules
     * type = 0 for reset
     * type = 1 for adding directory
     * type = 2 for deleting directory
     */
    @PostMapping("/post_ignore_directory_rules")
    public void manageIgnoreDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnorePaths();
            case 1 -> engineRules.addIgnorePaths(directory);
            case 2 -> engineRules.deleteIgnorePaths(directory);
        }
    }

    /**endpoint for modifying the root directory engine rules
     * type = 0 for reset
     * type = 1 for adding root directory
     * type = 2 for deleting root directory
     */
    @PostMapping("/post_root_directory_rules")
    public void manageRootDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetRootDirs();
            case 1 -> engineRules.addRootDirs(directory);
            case 2 -> engineRules.deleteRootDir(directory);
            case 3 -> engineRules.deleteAllRootDirs();
        }
    }

    /**endpoint for getting all the ignore extension engine rules*/
    @GetMapping("/get_ignore_extension_rules")
    public List<String> manageIgnoreExtensionRules(){
        return engineRules.getIgnoreExtensions();
    }

    /**endpoint for getting all the ignore directory engine rules*/
    @GetMapping("/get_ignore_directory_rules")
    public List<String> manageIgnoreDirectoryRules(){
        return engineRules.getIgnorePaths();
    }

    /**endpoint for getting all the root directory engine rules*/
    @GetMapping("/get_root_directory_rules")
    public List<String> manageRootDirectoryRules(){
        return engineRules.getRootDirs();
    }

    /**endpoint for getting the indexing report*/
    @GetMapping("/get_indexing_report")
    public IndexingStats getIndexingStats() {
        return indexingStats;
    }

    @PostMapping("/modify_ranking_algorithm")
    public void modifyRankingAlgorithm(@RequestParam String type){
        searchEngine.modifyRankingAlgorithm(type);
    }
}
