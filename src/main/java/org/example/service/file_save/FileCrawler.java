package org.example.service.file_save;

import org.apache.tika.Tika;
import org.example.model.FileInfo;
import org.example.model.FileType;
import org.example.model.TextualFileInfo;
import org.example.repository.FileInfoGetter;
import org.example.repository.FileRepository;
import org.example.repository.IFileInfoGetter;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileCrawler {
    private final Logger logger = LoggerFactory.getLogger(FileCrawler.class);
    private final HashMap<FileType, IRepository<Long, ? extends FileInfo>> repositories = new HashMap<>();
    private final FileSaver fileSaver;
    private final Tika tika;
    private AtomicInteger modifiedCount = new AtomicInteger(0);
    private AtomicInteger skippedCount = new AtomicInteger(0);
    private AtomicInteger newCount = new AtomicInteger(0);
    public FileCrawler() {
        FileRepository<TextualFileInfo, String> textualFileRepository = FileRepository.textual();
        repositories.put(FileType.TEXTUAL_FILE, textualFileRepository);
        tika = new Tika();
        fileSaver = new FileSaver();
    }

    public void storeFileSystemSnapshot(List<Path> rootDirs){
        fileSaver.deleteAllFilesNotPresent(repositories);
        rootDirs.forEach(this::crawlDirectory);
        logger.info("Added {} files to database as they were modified in the file system.", modifiedCount.get());
        logger.info("Skipped {} files as they are already in database.", skippedCount.get());
        logger.info("Added {} files to database as they are new in the file system", newCount.get());
    }

    private void crawlDirectory(Path root) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    process(file, attrs);
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
    private void process(Path file, BasicFileAttributes attrs) {
        try {
            FileType fileType = FileType.UNKNOWN;
            String type = tika.detect(file);
            if(type.startsWith("text")) {
                fileType = FileType.TEXTUAL_FILE;
            }
            switch (fileType) {
                case TEXTUAL_FILE -> {
                    TextualFileInfo textualFileInfo = new TextualFileInfo(file.toFile(), attrs);
                    @SuppressWarnings("unchecked")
                    IRepository<Long, TextualFileInfo> repo =
                            (IRepository<Long, TextualFileInfo>) repositories.get(FileType.TEXTUAL_FILE);
                    fileSaver.addFile(textualFileInfo, repo, newCount, modifiedCount, skippedCount);
                }
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
