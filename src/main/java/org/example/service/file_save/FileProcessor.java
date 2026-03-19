package org.example.service.file_save;

import org.apache.tika.Tika;
import org.example.model.FileInfo;
import org.example.model.FileType;
import org.example.model.TextualFileInfo;
import org.example.repository.FileRepository;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileProcessor {
    private final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    private final Map<FileType, IRepository<Long, ? extends FileInfo>> repositories;
    private final FileSaver fileSaver;
    private final Tika tika;

    public FileProcessor() {
        FileRepository<TextualFileInfo, String> textualFileRepository = FileRepository.textual();
        this.repositories = new HashMap<>();
        this.repositories.put(FileType.TEXTUAL_FILE, textualFileRepository);
        this.fileSaver = new FileSaver();
        this.tika = new Tika();
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
            }
        }
        catch (IOException e) {
            logger.warn("Failed to process file={}", file, e);
        }
    }
}
