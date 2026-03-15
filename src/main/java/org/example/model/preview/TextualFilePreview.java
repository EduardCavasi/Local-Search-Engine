package org.example.model.preview;

import lombok.Getter;
import org.example.model.preview.FilePreview;

@Getter
public class TextualFilePreview extends FilePreview {
    private String content;
    public TextualFilePreview(String fileName, String filePath, String content) {
        super(fileName, filePath);
        this.content = content;
    }
}