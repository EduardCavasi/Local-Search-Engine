package org.example.service;

import org.example.database.DatabaseConnection;
import org.example.model.FilePreview;
import org.example.model.search.SearchParams;
import org.example.model.search.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine {
    private static final Logger logger = LoggerFactory.getLogger(SearchEngine.class);


    private static final int PREVIEW_WORDS_BEFORE = 15;
    private static final int PREVIEW_WORDS_AFTER = 15;

    private SearchQuery buildSearchQuery(SearchParams params) {
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

        SearchQuery query = new SearchQuery(sql.toString(), parameters);
        logger.info("Search engine query: {} with {} parameters", query.getSql(), query.getParameters().size());
        return query;
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
            conditions.add("metadata.size = ?");
            parameters.add(params.getQuerySize());
        }
        if (params.getQueryCreated() != null) {
            conditions.add("metadata.creation_time = ?");
            parameters.add(toTimestamp(params.getQueryCreated()));
        }
        if (params.getQueryLastModified() != null) {
            conditions.add("metadata.last_modified_time = ?");
            parameters.add(toTimestamp(params.getQueryLastModified()));
        }
        if (params.getQueryLastAccessed() != null) {
            conditions.add("metadata.last_access_time = ?");
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

    public List<FilePreview> executeQuery(SearchParams params) {
        SearchQuery query = buildSearchQuery(params);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.getSql())) {
            query.bindParameters(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return buildPreviews(rs);
            }
        } catch (SQLException e) {
            logger.error("Search failed: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<FilePreview> buildPreviews(ResultSet rs) throws SQLException {
        List<FilePreview> previews = new ArrayList<>();
        while (rs.next()) {
            String fileName = getString(rs, "file_name");
            String parentPath = getString(rs, "parent_directory_path");
            String contentSnippet = getString(rs, "preview_content");

            contentSnippet = stripHeadlineMarkup(contentSnippet);
            

            String filePath = (parentPath != null && fileName != null)
                    ? parentPath + File.separator + fileName
                    : (fileName != null ? fileName : "");

            previews.add(new FilePreview(
                    fileName != null ? fileName : "",
                    filePath,
                    contentSnippet != null ? contentSnippet : ""
            ));
        }
        return previews;
    }

    private static String stripHeadlineMarkup(String headline) {
        if (headline == null) return "";
        return headline.replaceAll("<[^>]+>", "").replaceAll("&([^;]+);", " ").trim();
    }

    private static String getString(ResultSet rs, String columnLabel) {
        try {
            return rs.getString(columnLabel);
        } catch (SQLException e) {
            return null;
        }
    }
}
