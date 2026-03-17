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
    private boolean greaterSize = true;
    private Long querySize = -1L;
    private boolean lastModifiedAfter = true;
    private FileTime queryLastModified;
    private boolean createdAfter = true;
    private FileTime queryCreated;
    private boolean lastAccessedAfter = true;
    private FileTime queryLastAccessed;
    private boolean needsContent;
    private String queryContent;
    public static SearchParamsBuilder builder() {
        return new SearchParamsBuilder();
    }
}
