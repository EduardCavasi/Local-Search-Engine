package org.example.model.preview;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * File Information to be retrieved by the search engine when searching for TEXTUAL files
 */
@Getter
@NoArgsConstructor
public class TextualFilePreview extends FilePreview {
    private String content;
    public TextualFilePreview(String fileName, String filePath, String content) {
        super(fileName, filePath);
        this.content = content;
    }

    @Override
    public String toString() {
        return getFileName() + " " + getFilePath() + " " + getContent();
    }
}