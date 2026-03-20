package org.example.controller;

import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_save.FileCrawler;
import org.example.service.file_search.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class EndpointController {
    private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);
    private final SearchEngine searchEngine;
    private final FileCrawler fileCrawler;
    public EndpointController(FileCrawler fileCrawler, SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
        this.fileCrawler = fileCrawler;
    }

    @PostMapping("/index")
    public void startIndexing(@RequestBody List<Path> rootDirs){
        logger.info("Executing indexing for directories: {}", rootDirs);
        fileCrawler.storeFileSystemSnapshot(rootDirs);
        logger.info("Finished indexing for directories: {}", rootDirs);
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
}
