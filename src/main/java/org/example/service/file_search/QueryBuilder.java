package org.example.service.file_search;

import lombok.NoArgsConstructor;
import org.example.model.search.SearchParams;
import org.example.model.search.SearchQuery;

import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class QueryBuilder {
    private static final int PREVIEW_WORDS_BEFORE = 15;
    private static final int PREVIEW_WORDS_AFTER = 15;

    public SearchQuery buildSearchQuery(SearchParams params) {
        StringBuilder sql = new StringBuilder();
        boolean contentSearchWithQuery = params.isNeedsContent()
                && params.getQueryContent() != null && !params.getQueryContent().trim().isEmpty();

        if (contentSearchWithQuery) {
            sql.append("SELECT *, ts_headline('simple', content_info.raw_content, plainto_tsquery('simple', ?), ")
                    .append("'MaxWords=").append(PREVIEW_WORDS_BEFORE + PREVIEW_WORDS_AFTER + 10)
                    .append(", MinWords=15') AS preview_content FROM file_info\n");
        } else {
            sql.append("SELECT * FROM file_info\n");
        }
        if (params.isNeedsMetadata()) {
            sql.append(" JOIN metadata ON file_info.file_id = metadata.file_id\n");
        }
        if (params.isNeedsContent()) {
            sql.append(" JOIN content_info ON file_info.file_id = content_info.file_id\n");
        }

        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        appendFileInfoConditions(params, conditions, parameters);
        appendMetadataConditions(params, conditions, parameters);
        appendContentConditions(params, conditions, parameters);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(conditions.stream().collect(Collectors.joining(" AND ")));
        }

        if (contentSearchWithQuery) {
            parameters.add(params.getQueryContent().trim());
        }

        return new SearchQuery(sql.toString(), parameters);
    }

    private void appendFileInfoConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (params.getQueryFileName() != null && !params.getQueryFileName().isEmpty()) {
            conditions.add("file_info.file_name LIKE ?");
            parameters.add(params.getQueryFileName());
        }
        if (params.getQueryFileExtension() != null && !params.getQueryFileExtension().isEmpty()) {
            conditions.add("file_info.file_extension LIKE ?");
            parameters.add(params.getQueryFileExtension());
        }
        if (params.getQueryFilePath() != null && !params.getQueryFilePath().isEmpty()) {
            conditions.add("file_info.parent_directory_path LIKE ?");
            parameters.add(params.getQueryFilePath());
        }
    }

    private void appendMetadataConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (!params.isNeedsMetadata()) {
            return;
        }
        if (params.getQuerySize() != null && params.getQuerySize() != -1L) {
            if(params.isGreaterSize()) {
                conditions.add("metadata.size > ?");
            }
            else{
                conditions.add("metadata.size < ?");
            }
            parameters.add(params.getQuerySize());
        }
        if (params.getQueryCreated() != null) {
            if(params.isCreatedAfter()) {
                conditions.add("metadata.creation_time > ?");
            }
            else{
                conditions.add("metadata.creation_time < ?");
            }
            parameters.add(toTimestamp(params.getQueryCreated()));
        }
        if (params.getQueryLastModified() != null) {
            if(params.isLastModifiedAfter()) {
                conditions.add("metadata.last_modified_time > ?");
            }
            else{
                conditions.add("metadata.last_modified_time < ?");
            }
            parameters.add(toTimestamp(params.getQueryLastModified()));
        }
        if (params.getQueryLastAccessed() != null) {
            if(params.isLastAccessedAfter()) {
                conditions.add("metadata.last_access_time > ?");
            }
            else{
                conditions.add("metadata.last_access_time < ?");
            }
            parameters.add(toTimestamp(params.getQueryLastAccessed()));
        }
    }

    private void appendContentConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (!params.isNeedsContent()) {
            return;
        }
        if (params.getQueryContent() != null && !params.getQueryContent().isEmpty()) {
            conditions.add("content_info.searchable_content @@ plainto_tsquery('simple', ?)");
            parameters.add(params.getQueryContent());
        }
    }

    private static Timestamp toTimestamp(FileTime fileTime) {
        return fileTime == null ? null : new Timestamp(fileTime.toMillis());
    }

}
