package org.example.service.file_save;

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
    private final FileProcessor fileProcessorFactory;
    public FileCrawler() {
        this.fileProcessorFactory = new FileProcessor();
    }
    public void crawl(Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    fileProcessorFactory.process(file, attrs);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    logger.warn("File {} could not be visited", file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
