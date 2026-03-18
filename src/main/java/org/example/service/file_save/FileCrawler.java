package org.example.service.file_save;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileCrawler {
    private final Logger logger = LoggerFactory.getLogger(FileCrawler.class);
    private final FileProcessor fileProcessor;
    private final FileCrawlerStats stats;

    public FileCrawler() {
        this(new FileProcessor(), new FileCrawlerStats());
    }

    public FileCrawler(FileProcessor fileProcessor, FileCrawlerStats stats) {
        this.fileProcessor = fileProcessor;
        this.stats = stats;
    }

    public void storeFileSystemSnapshot(List<Path> rootDirs){
        fileProcessor.deleteAllFilesNotPresent(stats);
        rootDirs.forEach(this::crawlDirectory);

        logger.info("Deleted {} files from database as they are no longer in file system.", stats.getDeletedCount().get());
        logger.info("Added {} files to database as they were modified in the file system.", stats.getModifiedCount().get());
        logger.info("Skipped {} files as they are already in database.", stats.getSkippedCount().get());
        logger.info("Added {} files to database as they are new in the file system", stats.getModifiedCount().get());
    }

    private void crawlDirectory(Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    fileProcessor.processFile(file, attrs, stats);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    logger.error("File could not be visited file={}", file, e);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Failed to crawl directory root={}", root, e);
        }
    }
}