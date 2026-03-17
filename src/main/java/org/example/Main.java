package org.example;

import org.example.model.preview.FilePreview;
import org.example.model.preview.TextualFilePreview;
import org.example.model.search.SearchParams;
import org.example.service.file_save.FileCrawler;
import org.example.service.file_search.SearchEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        FileCrawler crawler = new FileCrawler();
        crawler.storeFileSystemSnapshot(List.of(Path.of("C:\\polibooks\\an3\\sem2\\software_engineering\\project\\test")));
        SearchParams params = SearchParams.builder()
                .needsContent(true)
                .setQueryContent("ana are mere")
                .build();
        SearchEngine se = new SearchEngine();
        List<FilePreview> previews = se.executeQuery(params).get();
        for (FilePreview preview : previews) {
            System.out.println(preview.getFilePath());
            if (preview instanceof TextualFilePreview) {
                System.out.println(((TextualFilePreview)preview).getContent());
            }
            System.out.println();
        }
    }
}