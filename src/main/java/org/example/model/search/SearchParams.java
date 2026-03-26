package org.example.model.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.model.search.utils.FileTimeJsonDeserializer;

import java.nio.file.attribute.FileTime;

/**
 * Class containing all the possible search parameters usable by the search engine
 * When the search uses file metadata the field needsMetadata = true
 * When the search uses file content the field needsContent = true
 */
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


    private boolean needsContent;
    private String queryContent;
}
