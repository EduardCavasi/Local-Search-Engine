package org.example.model.preview;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FilePreview {
    private final String fileName;
    private final String filePath;
}
