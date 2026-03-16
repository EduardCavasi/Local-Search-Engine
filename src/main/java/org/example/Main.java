package org.example;

import org.example.model.preview.FilePreview;
import org.example.model.preview.TextualFilePreview;
import org.example.model.search.SearchParams;
import org.example.service.FileCrawler;
import org.example.service.SearchEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        FileCrawler crawler = new FileCrawler();
        crawler.crawl(Path.of("src"));
        SearchParams params = SearchParams.builder()
                .needsContent(true)
                .setQueryContent("ana are mere")
                .build();
        SearchEngine se = new SearchEngine();
        List<FilePreview> previews = se.executeQuery(params);
        for (FilePreview preview : previews) {
            System.out.println(preview.getFilePath());
            if (preview instanceof TextualFilePreview) {
                System.out.println(((TextualFilePreview)preview).getContent());
            }
            System.out.println();
        }
    }
}