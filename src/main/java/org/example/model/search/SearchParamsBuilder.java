package org.example.model.search;

import java.nio.file.attribute.FileTime;
import java.security.Timestamp;

public class SearchParamsBuilder {
    private SearchParams searchParams = new SearchParams();

    public SearchParamsBuilder setQueryFileName(String queryFileName) {
        searchParams.setQueryFileName(queryFileName);
        return this;
    }

    public SearchParamsBuilder setQueryFileExtension(String queryFileExtension) {
        searchParams.setQueryFileExtension(queryFileExtension);
        return this;
    }

    public SearchParamsBuilder setQueryFilePath(String queryFilePath) {
        searchParams.setQueryFilePath(queryFilePath);
        return this;
    }

    public SearchParamsBuilder needsMetadata(Boolean needsMetadata) {
        searchParams.setNeedsMetadata(needsMetadata);
        return this;
    }

    public SearchParamsBuilder setCreated(FileTime created) {
        searchParams.setQueryCreated(created);
        return this;
    }

    public SearchParamsBuilder setLastAccessed(FileTime lastAccessed) {
        searchParams.setQueryLastAccessed(lastAccessed);
        return this;
    }

    public SearchParamsBuilder setLastModified(FileTime lastModified) {
        searchParams.setQueryLastModified(lastModified);
        return this;
    }

    public SearchParamsBuilder needsContent(Boolean needsContent) {
        searchParams.setNeedsContent(needsContent);
        return this;
    }

    public SearchParamsBuilder setQueryContent(String queryContent) {
        searchParams.setQueryContent(queryContent);
        return this;
    }

    public SearchParamsBuilder setGreaterSize(boolean greaterSize) {
        searchParams.setGreaterSize(greaterSize);
        return this;
    }
    public SearchParamsBuilder setLastAccessedAfter(boolean lastAccessedAfter) {
        searchParams.setLastAccessedAfter(lastAccessedAfter);
        return this;
    }
    public SearchParamsBuilder setCreatedAfter(boolean createdAfter) {
        searchParams.setCreatedAfter(createdAfter);
        return this;
    }
    public SearchParamsBuilder setLastModifiedAfter(boolean lastModifiedAfter) {
        searchParams.setLastModifiedAfter(lastModifiedAfter);
        return this;
    }

    public SearchParams build(){
        return this.searchParams;
    }
}
