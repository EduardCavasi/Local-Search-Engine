package org.example.repository.history;

import org.example.database.IDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SearchResultsPersistence {
    private static final Logger logger = LoggerFactory.getLogger(SearchResultsPersistence.class);
    private final IDataSource dataSource;

    private static final String INSERT_SQL =
            "INSERT INTO search_result_history (file_path, timestamp, apparitions) " +
                    "VALUES (?, ?, 1) " +
                    "ON CONFLICT (file_path) DO UPDATE SET " +
                    "apparitions = search_result_history.apparitions + 1, " +
                    "timestamp = EXCLUDED.timestamp";

    private static final String DELETE_ALL_SQL =
            "TRUNCATE TABLE search_result_history";

    private static final String GET_TOP_SEARCHES =
            "SELECT file_path " +
                    "FROM search_result_history " +
                    "ORDER BY apparitions DESC " +
                    "LIMIT ?";

    public SearchResultsPersistence(IDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(String filePath, Timestamp timestamp) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, filePath);
            ps.setTimestamp(2, timestamp);

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.warn("Error saving search result history", e);
            return;
        }
        logger.info("Inserted result {} into search_request_history", filePath);
    }

    public void deleteAll() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ALL_SQL)) {

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.warn("Error deleting all search result history", e);
            return;
        }
        logger.info("Deleted all history results");
    }

    public List<String> getTopEntries(int nrResults) {
        List<String> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_TOP_SEARCHES)) {

            ps.setInt(1, nrResults);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("file_path"));
                }
            }

        } catch (SQLException e) {
            logger.warn("Error fetching top search results", e);
        }
        logger.info("Retrieved top {} search results", nrResults);
        return results;
    }
}