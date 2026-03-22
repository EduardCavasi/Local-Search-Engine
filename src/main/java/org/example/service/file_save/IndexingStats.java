package org.example.service.file_save;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Getter
public class IndexingStats {
    private static final Logger logger = LoggerFactory.getLogger(IndexingStats.class);
    private final AtomicInteger modifiedCount;
    private final AtomicInteger skippedCount;
    private final AtomicInteger newCount;
    private final AtomicInteger deletedCount;
    private final AtomicInteger errorCount;
    private final AtomicInteger ignoredCount;
    private final AtomicInteger nonTextualCount;
    public IndexingStats() {
        modifiedCount = new AtomicInteger(0);
        skippedCount = new AtomicInteger(0);
        newCount = new AtomicInteger(0);
        deletedCount = new AtomicInteger(0);
        errorCount = new AtomicInteger(0);
        ignoredCount = new AtomicInteger(0);
        nonTextualCount = new AtomicInteger(0);
    }
    private void log(){
        logger.info("Deleted {} files from database as they are no longer in file system.", deletedCount.get());
        logger.info("Added {} files to database as they were modified in the file system.", modifiedCount.get());
        logger.info("Skipped {} files as they are already in database.", skippedCount.get());
        logger.info("Added {} files to database as they are new in the file system", newCount.get());
        logger.info("Error processing {} files.", errorCount.get());
        logger.info("Ignored {} files.", ignoredCount.get());
        logger.info("NonTextual {} files.", nonTextualCount.get());
    }
    private void reset(){
        deletedCount.set(0);
        modifiedCount.set(0);
        skippedCount.set(0);
        newCount.set(0);
        errorCount.set(0);
        ignoredCount.set(0);
        nonTextualCount.set(0);
    }
    public void report(){
        logger.info("---------------------------FINISHED INDEXING---------------------------");
        log();
        reset();
        logger.info("---------------------------END OF INDEXING---------------------------");

    }
    public void progress(int nrFiles){
        logger.info("---------------------------Processed {} files---------------------------", nrFiles);
        log();
        logger.info("------------------------------------------------------------------------");
    }
    public void incrementModifiedCount(){
        modifiedCount.incrementAndGet();
    }
    public void incrementSkippedCount(){
        skippedCount.incrementAndGet();
    }
    public void incrementNewCount(){
        newCount.incrementAndGet();
    }
    public void incrementDeletedCount(){
        deletedCount.incrementAndGet();
    }
    public void incrementErrorCount(){
        errorCount.incrementAndGet();
    }
    public void incrementIgnoredCount(){
        ignoredCount.incrementAndGet();
    }
    public void incrementNonTextualCount(){
        nonTextualCount.incrementAndGet();
    }
}

