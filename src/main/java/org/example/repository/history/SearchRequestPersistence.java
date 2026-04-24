package org.example.repository.history;

import org.example.database.IDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchRequestPersistence{
    private static final Logger logger = LoggerFactory.getLogger(SearchRequestPersistence.class);
    private final IDataSource dataSource;

    private static final String INSERT_SQL =
            "INSERT INTO search_request_history (request, timestamp) VALUES (?, ?)";
    private static final String DELETE_ALL_SQL =
            "TRUNCATE TABLE search_request_history";
    private static final String GET_TOP_SEARCHES =
            "SELECT request, COUNT(*) AS cnt FROM search_request_history GROUP BY request ORDER BY cnt DESC LIMIT ?";
    private static final String GET_TOP_SEARCH_SUGGESTIONS =
            "SELECT request, COUNT(*) AS cnt FROM search_request_history WHERE request ILIKE ? GROUP BY request ORDER BY cnt DESC LIMIT ?";
    public SearchRequestPersistence(IDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(String history, Timestamp timestamp) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setString(1, history);
            stmt.setTimestamp(2, timestamp);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("Could not insert history");
                return;
            }
        } catch (SQLException e) {
            logger.warn("Could not insert history", e);
            return;
        }
        logger.info("Inserted request {} into search_request_history", history);
    }

    public void deleteAll() {
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(DELETE_ALL_SQL);
        ){
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Could not delete history", e);
            return;
        }
        logger.info("Deleted all history requests");
    }

    public Map<String, Long> getTopEntries(int nrResults) {
        Map<String, Long> entries = new HashMap<>();
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(GET_TOP_SEARCHES);
        ){
            stmt.setInt(1, nrResults);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String request = rs.getString("request");
                long count = rs.getLong("cnt");
                entries.put(request, count);
            }
        } catch (SQLException e) {
            logger.warn("Could not retrieve top search request", e);
            return Map.of();
        }
        logger.info("Retrieved top {} search request", nrResults);
        return entries;
    }

    public List<String> getTopSuggestions(int nrResults, String currentInput) {
        List<String> entries = new ArrayList<>();
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(GET_TOP_SEARCH_SUGGESTIONS);
        ){
            stmt.setString(1, "%"+currentInput+"%");
            stmt.setInt(2, nrResults);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String request = rs.getString("request");
                entries.add(request);
            }
        } catch (SQLException e) {
            logger.warn("Could not retrieve top search suggestions", e);
            return List.of();
        }
        logger.info("Retrieved top {} search suggestions", nrResults);
        return entries;
    }
}
