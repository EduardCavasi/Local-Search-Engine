package org.example.controller;

import org.example.model.general.EngineRules;
import org.example.service.file_save.IndexingService;
import org.example.service.file_save.IndexingStats;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "http://localhost:1420")
public class SystemController {

    private final IndexingService indexingService;
    private final EngineRules engineRules;
    private final IndexingStats indexingStats;

    public SystemController(IndexingService indexingService, EngineRules engineRules, IndexingStats indexingStats) {
        this.indexingService = indexingService;
        this.engineRules = engineRules;
        this.indexingStats = indexingStats;
    }

    /**endpoint for starting indexing*/
    @PostMapping("/index")
    public void startIndexing(){
        indexingService.storeFileSystemSnapshot();
    }

    /**endpoint for getting all the ignore extension engine rules*/
    @GetMapping("ignore_extension_rules")
    public List<String> manageIgnoreExtensionRules(){
        return engineRules.getIgnoreExtensions();
    }

    /**endpoint for modifying the ignore extensions engine rules
     * type = 0 for reset
     * type = 1 for adding extension
     * type = 2 for deleting extension
     */
    @PostMapping("/ignore_extension_rules")
    public void manageIgnoreExtensionRules(@RequestParam String extension, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnoreExtensions();
            case 1 -> engineRules.addIgnoreExtensions(extension);
            case 2 -> engineRules.deleteIgnoreExtensions(extension);
        }
    }

    /**endpoint for getting all the ignore directory engine rules*/
    @GetMapping("/ignore_directory_rules")
    public List<String> manageIgnoreDirectoryRules(){
        return engineRules.getIgnorePaths();
    }

    /**endpoint for modifying the ignore directory engine rules
     * type = 0 for reset
     * type = 1 for adding directory
     * type = 2 for deleting directory
     */
    @PostMapping("/ignore_directory_rules")
    public void manageIgnoreDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnorePaths();
            case 1 -> engineRules.addIgnorePaths(directory);
            case 2 -> engineRules.deleteIgnorePaths(directory);
        }
    }

    /**endpoint for getting all the root directory engine rules*/
    @GetMapping("/root_directory_rules")
    public List<String> manageRootDirectoryRules(){
        return engineRules.getRootDirs();
    }

    /**endpoint for modifying the root directory engine rules
     * type = 0 for reset
     * type = 1 for adding root directory
     * type = 2 for deleting root directory
     */
    @PostMapping("/root_directory_rules")
    public void manageRootDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetRootDirs();
            case 1 -> engineRules.addRootDirs(directory);
            case 2 -> engineRules.deleteRootDir(directory);
            case 3 -> engineRules.deleteAllRootDirs();
        }
    }

    /**endpoint for getting the indexing report*/
    @GetMapping("/indexing_report")
    public IndexingStats getIndexingStats() {
        return indexingStats;
    }

}
