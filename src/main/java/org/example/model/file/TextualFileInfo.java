package org.example.model.file;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * FileInfo object for TEXTUAL files
 * Besides the FileInfo fields it contains a content field containing the raw text from the file
 * If the file can not be read a warning is logged and the content is initialized as empty
 */
@Getter
@Setter
public class TextualFileInfo extends FileInfo {
    private final Logger logger = LoggerFactory.getLogger(TextualFileInfo.class);
    private String content;
    public TextualFileInfo(File file, BasicFileAttributes attr) {
        super(file, attr);
        this.content = this.readContent(file);
        super.setFileType(FileType.TEXTUAL_FILE);
    }
    private String readContent(File file) {
        String content = "";
        try {
            content = Files.readString(file.toPath());
        }
        catch (IOException e) {
            logger.warn("Unable to read file: {}", file.getAbsolutePath());
        }
        return content;
    }
}
