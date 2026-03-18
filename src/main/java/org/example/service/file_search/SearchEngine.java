package org.example.service.file_search;

import org.example.database.DatabaseConnection;
import org.example.database.IDataSource;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchParams;
import org.example.model.search.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class SearchEngine {
    private static final Logger logger = LoggerFactory.getLogger(SearchEngine.class);
    private final IDataSource dataSource;
    private final PreviewBuilder previewBuilder;
    private final QueryBuilder queryBuilder;

    public SearchEngine() {
        this(DatabaseConnection.getInstance(), new PreviewBuilder(), new QueryBuilder());
    }

    public SearchEngine(IDataSource dataSource, PreviewBuilder previewBuilder, QueryBuilder queryBuilder) {
        this.dataSource = dataSource;
        this.previewBuilder = previewBuilder;
        this.queryBuilder = queryBuilder;
    }

    public Optional<List<FilePreview>> executeQuery(SearchParams params) {
        SearchQuery query = queryBuilder.buildSearchQuery(params);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.getSql())) {
            query.bindParameters(ps);
            ResultSet rs = ps.executeQuery();
            return Optional.of(previewBuilder.buildPreviews(rs));
        } catch (SQLException e) {
            logger.error("Search failed", e);
        }
        return Optional.empty();
    }

}
