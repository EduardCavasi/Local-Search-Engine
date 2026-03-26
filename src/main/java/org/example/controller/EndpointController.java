package org.example.controller;

import org.example.model.general.EngineRules;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_save.FileCrawler;
import org.example.service.file_save.IndexingStats;
import org.example.service.file_search.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final FileCrawler fileCrawler;
    private final EngineRules engineRules;
    private final IndexingStats indexingStats;
    public EndpointController(FileCrawler fileCrawler, SearchEngine searchEngine, EngineRules engineRules, IndexingStats indexingStats) {
        this.searchEngine = searchEngine;
        this.fileCrawler = fileCrawler;
        this.engineRules = engineRules;
        this.indexingStats = indexingStats;
    }

    /**endpoint for starting indexing*/
    @PostMapping("/index")
    public void startIndexing(){
        fileCrawler.storeFileSystemSnapshot();
    }

    /**endpoint for searching by receiving a SearchParams object*/
    @PostMapping("/search")
    public List<FilePreview> executeSearchQuery(@RequestBody SearchParams searchParams){
        logger.info("Executing search query: {}", searchParams);
        Optional<List<FilePreview>> previews = searchEngine.executeQuery(searchParams);
        List<FilePreview> ans = new ArrayList<>();
        if(previews.isPresent()){
            ans = previews.get();
        }
        logger.info("Finished search query : {}, returning {} results", searchParams, ans.size());
        return ans;
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
        }
    }

    /**endpoint for getting all the ignore extension engine rules*/
    @PostMapping("/get_ignore_extension_rules")
    public List<String> manageIgnoreExtensionRules(){
        return engineRules.getIgnoreExtensions();
    }

    /**endpoint for getting all the ignore directory engine rules*/
    @PostMapping("/get_ignore_directory_rules")
    public List<String> manageIgnoreDirectoryRules(){
        return engineRules.getIgnorePaths();
    }

    /**endpoint for getting all the root directory engine rules*/
    @PostMapping("/get_root_directory_rules")
    public List<String> manageRootDirectoryRules(){
        return engineRules.getRootDirs();
    }

    /**endpoint for getting the indexing report*/
    @GetMapping("/get_indexing_report")
    public IndexingStats getIndexingStats() {
        return indexingStats;
    }
}
