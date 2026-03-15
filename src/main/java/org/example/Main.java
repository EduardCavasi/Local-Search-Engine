package org.example;

import org.example.model.FilePreview;
import org.example.model.TextualFileInfo;
import org.example.model.search.SearchParams;
import org.example.repository.TextualFileRepository;
import org.example.service.FileCrawler;
import org.example.service.SearchEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        FileCrawler crawler = new FileCrawler();
        crawler.crawl(Path.of("C:\\polibooks\\an3\\sem2\\software_engineering\\project\\search_engine_core\\src"));
        SearchParams params = SearchParams.builder()
                .needsContent(true)
                .setQueryContent("ana are mere")
                .build();
        SearchEngine se = new SearchEngine();
        List<FilePreview> previews = se.executeQuery(params);
        for (FilePreview preview : previews) {
            System.out.println(preview.getFilePath());
            System.out.println(preview.getContent());
            System.out.println();
        }
    }
}