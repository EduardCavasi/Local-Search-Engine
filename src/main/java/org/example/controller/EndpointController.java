package org.example.controller;

import org.example.model.general.EngineRules;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_save.FileCrawler;
import org.example.service.file_search.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class EndpointController {
    private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);
    private final SearchEngine searchEngine;
    private final FileCrawler fileCrawler;
    private final EngineRules engineRules;
    public EndpointController(FileCrawler fileCrawler, SearchEngine searchEngine, EngineRules engineRules) {
        this.searchEngine = searchEngine;
        this.fileCrawler = fileCrawler;
        this.engineRules = engineRules;
    }

    @PostMapping("/index")
    public void startIndexing(){
        fileCrawler.storeFileSystemSnapshot();
    }

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

    @PostMapping("/ignore_extension_rules")
    public void manageIgnoreExtensionRules(@RequestParam String extension, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnoreExtensions();
            case 1 -> engineRules.addIgnoreExtensions(extension);
            case 2 -> engineRules.deleteIgnoreExtensions(extension);
        }
    }

    @PostMapping("/ignore_directory_rules")
    public void manageIgnoreDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetIgnorePaths();
            case 1 -> engineRules.addIgnorePaths(directory);
            case 2 -> engineRules.deleteIgnorePaths(directory);
        }
    }

    @PostMapping("/root_directory_rules")
    public void manageRootDirectoryRules(@RequestParam String directory, @RequestParam int type){
        switch(type){
            case 0 -> engineRules.resetRootDirs();
            case 1 -> engineRules.addRootDirs(directory);
            case 2 -> engineRules.deleteRootDir(directory);
        }
    }

}
