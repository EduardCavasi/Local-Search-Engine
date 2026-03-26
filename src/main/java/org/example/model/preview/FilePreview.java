package org.example.model.preview;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * File information to be retrieved by the search engine
 */
@Getter
@AllArgsConstructor
public class FilePreview {
    private final String fileName;
    private final String filePath;
}
