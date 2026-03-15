package org.example.model.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.attribute.FileTime;

@NoArgsConstructor
@Getter
@Setter
public class SearchParams {
    private String queryFileName;
    private String queryFileExtension;
    private String queryFilePath;
    private boolean needsMetadata;
    private Long querySize = -1L;
    private FileTime queryLastModified;
    private FileTime queryCreated;
    private FileTime queryLastAccessed;
    private boolean needsContent;
    private String queryContent;
    public static SearchParamsBuilder builder() {
        return new SearchParamsBuilder();
    }
}
