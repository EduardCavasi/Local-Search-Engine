package org.example.service.file_search;

import org.example.database.IDataSource;
import org.example.model.preview.FilePreview;
import org.example.model.search.SearchEvent;
import org.example.model.search.SearchParams;
import org.example.model.search.SearchQuery;
import org.example.service.file_search.ranking.AlphabeticRanking;
import org.example.service.file_search.ranking.CombinedRanking;
import org.example.service.file_search.ranking.LastModifiedRanking;
import org.example.service.file_search.ranking.RankingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The class that executes the query corresponding to a SearchParams object
 * FLOW:
 * input: SearchParams object
 * The input is sent to QueryBuilder => SearchQuery
 * A connection to the database is made and a statement is constructed based on the SearchQuery's body
 * The parameters of the SearchQuery object are bound to the statement
 * The statement is executed => ResultSet
 * The ResultSet is sent to PreviewBuilder => List of FilePreview
 */
@Service
public class SearchEngine {
    private static final Logger logger = LoggerFactory.getLogger(SearchEngine.class);
    private final IDataSource dataSource;
    private final PreviewBuilder previewBuilder;
    private final QueryBuilder queryBuilder;

    private final ApplicationEventPublisher eventPublisher;

    public SearchEngine(IDataSource dataSource, PreviewBuilder previewBuilder, QueryBuilder queryBuilder, ApplicationEventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.previewBuilder = previewBuilder;
        this.queryBuilder = queryBuilder;
        this.eventPublisher = eventPublisher;
    }

    public Optional<List<FilePreview>> executeQuery(SearchParams params) {
        SearchQuery query = queryBuilder.buildSearchQuery(params);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.getSql())) {
            query.bindParameters(ps);
            logger.info("Executing query:\n{}", ps);
            ResultSet rs = ps.executeQuery();
            var previews = previewBuilder.buildPreviews(rs);

            ///notify observers to build search history
            notifyObservers(params, previews);

            return Optional.of(previews);
        } catch (SQLException e) {
            logger.error("Search failed", e);
        }
        return Optional.empty();
    }

    public void modifyRankingAlgorithm(String type){
        RankingStrategy strategy;
        switch (type){
            case "alphabetic" -> strategy = new AlphabeticRanking();
            case "last_modified" -> strategy = new LastModifiedRanking();
            default ->  strategy = new CombinedRanking();
        }
        queryBuilder.setRankingStrategy(strategy);
    }

    private void notifyObservers(SearchParams params, List<FilePreview> previews) {
        List<String> filePaths = previews.stream().map(FilePreview::getFilePath).toList();
        SearchEvent event = new SearchEvent(params.getSearchRequest(), filePaths, Timestamp.from(Instant.now()));
        eventPublisher.publishEvent(event);
    }
}
