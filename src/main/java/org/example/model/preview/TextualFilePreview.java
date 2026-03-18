package org.example.model.preview;

import lombok.Getter;

@Getter
public class TextualFilePreview extends FilePreview {
    private final String content;
    public TextualFilePreview(String fileName, String filePath, String content) {
        super(fileName, filePath);
        this.content = content;
    }
}