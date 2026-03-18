package org.example.service.file_save;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class FileCrawlerStats {
    private final AtomicInteger modifiedCount;
    private final AtomicInteger skippedCount;
    private final AtomicInteger newCount;
    private final AtomicInteger deletedCount;
    public FileCrawlerStats() {
        modifiedCount = new AtomicInteger(0);
        skippedCount = new AtomicInteger(0);
        newCount = new AtomicInteger(0);
        deletedCount = new AtomicInteger(0);
    }
}
