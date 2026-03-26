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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main responsible for file indexing
 * Crawls through root directories and reconciles the file system with the database
 */
@Service
public class IndexingService {
    private final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private final FileProcessor fileProcessor;
    private final EngineRules engineRules;
    private final IndexingStats stats;
    private final IndexingJobId indexingJobId;
    public IndexingService(FileProcessor fileProcessor, EngineRules engineRules, IndexingStats stats, IndexingJobId indexingJobId) {
        this.fileProcessor = fileProcessor;
        this.engineRules = engineRules;
        this.stats = stats;
        this.indexingJobId = indexingJobId;
    }

    /**
     * Method for reconciling the root directories with what we already have in the database(incremental indexing)
     * Creates a background thread for indexing which does 2 things:
     * 1. uses fileProcessor.deleteAllFilesNotPresent(stats); to delete all the files present in the database, but deleted from the file system
     * 2. Creates a thread for crawling through each root directory from EngineRules
     */
    public void storeFileSystemSnapshot(){
        //increment the scan ID
        indexingJobId.incrementJobId();
        AtomicInteger fileCount = new AtomicInteger(0);
        List<Path> rootDirs = engineRules.getRootDirs().stream().map(Path::of).toList();
        logger.info("Executing indexing for directories: {}", rootDirs);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(() -> {
            ExecutorService workers = Executors.newFixedThreadPool(4);
            try {
                List<Callable<Void>> tasks = rootDirs.stream().map(
                        dir -> (Callable<Void>) () -> {
                            crawlDirectory(dir, fileCount);
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
                fileProcessor.deleteAllFilesNotPresent(stats, indexingJobId.getScanId());
                stats.report();
            }
        });
        try{
            future.get();
        }
        catch(InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        executorService.shutdown();
        logger.info("Finished indexing for directories: {}", rootDirs);
    }

    /**
     * Method for crawling recursively through a root directory
     * Each file is processed using fileProcessor.processFile(file, attrs, stats);
     * Cuts the crawling for directories to be ignored(from EngineRules)
     * Identifies SYMLINK LOOPS and UNACCESSIBLE FILES and logs a warning for each of them
     * Logs the indexing statistics(IndexingStats) every 100 processed files.
     */
    private void crawlDirectory(Path root, AtomicInteger fileCount) {
        try {
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE ,new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    if(engineRules.continueIndexFile(file, attrs)) {
                        int count = fileCount.incrementAndGet();
                        if(count % 100 == 0){
                            stats.progress(count);
                        }
                        fileProcessor.processFile(file, attrs, stats, indexingJobId.getScanId());
                    }
                    else{
                        stats.incrementIgnoredCount();
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
                    stats.incrementErrorCount();
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