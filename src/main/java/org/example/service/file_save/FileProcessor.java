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

@Service
public class FileProcessor {
    private final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    private final Map<FileType, IRepository<Long, ? extends FileInfo>> repositories;
    private final FileSaver fileSaver;
    private final Tika tika;

    public FileProcessor(IRepository<Long, TextualFileInfo> textualFileRepository, FileSaver fileSaver, Tika tika) {
        this.repositories = new HashMap<>();
        this.repositories.put(FileType.TEXTUAL_FILE, textualFileRepository);
        this.fileSaver = fileSaver;
        this.tika = tika;
    }

    public void deleteAllFilesNotPresent(IndexingStats stats){
        fileSaver.deleteAllFilesNotPresent(repositories, stats);
    }

    public void processFile(Path file, BasicFileAttributes attrs, IndexingStats stats){
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
                    fileSaver.addFile(textualFileInfo, repo, stats);
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
