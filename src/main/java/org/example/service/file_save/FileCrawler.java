package org.example.service.file_save;

import org.example.model.general.EngineRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileCrawler {
    private final Logger logger = LoggerFactory.getLogger(FileCrawler.class);
    private final FileProcessor fileProcessor;
    private final EngineRules engineRules;
    private final IndexingStats stats;
    public FileCrawler(FileProcessor fileProcessor, EngineRules engineRules, IndexingStats stats) {
        this.fileProcessor = fileProcessor;
        this.engineRules = engineRules;
        this.stats = stats;
    }

    public void storeFileSystemSnapshot(){
        List<Path> rootDirs = engineRules.getRootDirs().stream().map(Path::of).toList();
        logger.info("Executing indexing for directories: {}", rootDirs);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            ExecutorService workers = Executors.newFixedThreadPool(4);
            try {
                fileProcessor.deleteAllFilesNotPresent(stats);

                List<Callable<Void>> tasks = rootDirs.stream().map(
                        dir -> (Callable<Void>) () -> {
                            crawlDirectory(dir);
                            return null;
                        }
                ).toList();
                workers.invokeAll(tasks);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finally {
                workers.shutdown();
                executorService.shutdown();
                logger.info("Deleted {} files from database as they are no longer in file system.", stats.getDeletedCount().get());
                logger.info("Added {} files to database as they were modified in the file system.", stats.getModifiedCount().get());
                logger.info("Skipped {} files as they are already in database.", stats.getSkippedCount().get());
                logger.info("Added {} files to database as they are new in the file system", stats.getNewCount().get());
                stats.getDeletedCount().set(0);
                stats.getModifiedCount().set(0);
                stats.getSkippedCount().set(0);
                stats.getNewCount().set(0);
            }
        });
        executorService.shutdown();
        logger.info("Finished indexing for directories: {}", rootDirs);
    }

    private void crawlDirectory(Path root) {
        try {
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE ,new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    if(engineRules.continueIndexFile(file)) {
                        fileProcessor.processFile(file, attrs, stats);
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    if(e instanceof FileSystemLoopException) {
                        logger.error("Symlink loop detected: {}", file);
                    }
                    else{
                        logger.error("File could not be visited file={}", file, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!engineRules.continueIndexDirectory(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            logger.error("Failed to crawl directory root={}", root, e);
        }
    }
}