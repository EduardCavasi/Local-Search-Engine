package org.example.model.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.model.search.utils.FileTimeJsonDeserializer;

import java.nio.file.attribute.FileTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class SearchParams {
    private String queryFileName;
    private String queryFileExtension;
    private String queryFilePath;
    private boolean needsMetadata;
    private boolean greaterSize = true;
    private Long querySize = -1L;
    private boolean lastModifiedAfter = true;

    @JsonDeserialize(using = FileTimeJsonDeserializer.class)
    private FileTime queryLastModified;

    private boolean createdAfter = true;

    @JsonDeserialize(using = FileTimeJsonDeserializer.class)
    private FileTime queryCreated;

    private boolean lastAccessedAfter = true;

    @JsonDeserialize(using = FileTimeJsonDeserializer.class)
    private FileTime queryLastAccessed;

    private boolean needsContent;
    private String queryContent;
}
