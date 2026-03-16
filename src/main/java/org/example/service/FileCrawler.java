package org.example.service;

import org.example.model.TextualFileInfo;
import org.example.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileCrawler {
    private final Logger logger = LoggerFactory.getLogger(FileCrawler.class);
    private final FileRepository<TextualFileInfo, String> textualFileRepository;
    public FileCrawler() {
        textualFileRepository = FileRepository.textual();
    }

    public void crawl(Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    TextualFileInfo fileInfo = new TextualFileInfo(file.toFile(), attrs);
                    textualFileRepository.save(fileInfo);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    logger.warn("File could not be visited");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
