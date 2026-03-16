package org.example.model;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

@Getter
public class TextualFileInfo extends FileInfo {
    private final Logger logger = LoggerFactory.getLogger(TextualFileInfo.class);
    private final String content;
    public TextualFileInfo(File file, BasicFileAttributes attr) {
        super(file, attr);
        this.content = this.readContent(file);
        super.setFileType(FileType.TEXTUAL_FILE);
        logger.info("Created textual file object " + file.getAbsolutePath());
    }
    private String readContent(File file) {
        String content = null;
        try {
            content = Files.readString(file.toPath());
        }
        catch (IOException e) {
            logger.warn(e.getMessage());
        }
        return content;
    }
}
