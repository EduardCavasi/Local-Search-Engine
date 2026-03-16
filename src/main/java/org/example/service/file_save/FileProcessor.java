package org.example.service.file_save;

import org.apache.tika.Tika;
import org.example.model.FileType;
import org.example.model.TextualFileInfo;
import org.example.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    private final FileRepository<TextualFileInfo, String> textualFileRepository;
    private final Tika tika;
    public FileProcessor() {
        textualFileRepository = FileRepository.textual();
        tika = new Tika();
    }

    public void process(Path file, BasicFileAttributes attrs) {
        try {
            FileType fileType = FileType.UNKNOWN;
            String type = tika.detect(file);
            if(type.startsWith("text")) {
                fileType = FileType.TEXTUAL_FILE;
            }
            switch (fileType) {
                case TEXTUAL_FILE -> {
                    TextualFileInfo fileInfo = new TextualFileInfo(file.toFile(), attrs);
                    textualFileRepository.save(fileInfo);
                }
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
