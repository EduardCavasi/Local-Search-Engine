package org.example.service.file_save;

import org.apache.tika.Tika;
import org.example.model.file.FileInfo;
import org.example.model.file.FileType;
import org.example.model.file.TextualFileInfo;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for: indexing a file and deleting files from database
 * Uses FileSaver capabilities for deleting and saving.
 */
@Service
public class FileProcessor {
    private final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    /**maps each file type with the corresponding repository*/
    private final Map<FileType, IRepository<Long, ? extends FileInfo>> repositories;
    private final FileSaver fileSaver;
    private final Tika tika;

    public FileProcessor(IRepository<Long, TextualFileInfo> textualFileRepository, FileSaver fileSaver, Tika tika) {
        this.repositories = new HashMap<>();
        this.repositories.put(FileType.TEXTUAL_FILE, textualFileRepository);
        this.fileSaver = fileSaver;
        this.tika = tika;
    }

    /**deletes all files present in DB but not in file system*/
    public void deleteAllFilesNotPresent(IndexingStats stats, Long scanId){
        fileSaver.deleteAllFilesNotPresent(stats, scanId);
    }

    /**method which delegates the saving responsibility for each type of file to the corresponding repository*/
    public void processFile(Path file, BasicFileAttributes attrs, IndexingStats stats, Long scanId){
        try {
            FileType fileType = FileType.UNKNOWN;
            String type = tika.detect(file);
            if(type.startsWith("text")) {
                fileType = FileType.TEXTUAL_FILE;
            }
            switch (fileType) {
                case TEXTUAL_FILE -> {
                    TextualFileInfo textualFileInfo = new TextualFileInfo(file.toFile(), attrs, scanId);
                    @SuppressWarnings("unchecked")
                    IRepository<Long, TextualFileInfo> repo =
                            (IRepository<Long, TextualFileInfo>) repositories.get(FileType.TEXTUAL_FILE);
                    fileSaver.addFile(textualFileInfo, repo, stats, scanId);
                }
                default -> stats.incrementNonTextualCount();
            }
        }
        catch (IOException e) {
            logger.warn("Failed to process file={}", file, e);
            stats.incrementNonTextualCount();
        }
    }
}
