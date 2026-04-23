package org.example.model.preview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * File information to be retrieved by the search engine
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FilePreview {
    private String fileName;
    private String filePath;
}
