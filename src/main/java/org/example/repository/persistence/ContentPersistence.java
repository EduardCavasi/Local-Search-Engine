package org.example.repository.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository implementation for accessing content_info table
 */
@Component
public class ContentPersistence implements IPersistence<Long, String> {
    private static final Logger logger = LoggerFactory.getLogger(ContentPersistence.class);
    private static final String INSERT_SQL =
            "INSERT INTO content_info (file_id, raw_content) VALUES (?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM content_info WHERE file_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE content_info SET raw_content = ? WHERE file_id = ?";
    @Override
    public Optional<Long> save(Connection conn, Long id, String rawContent) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setLong(1, id);
            stmt.setString(2, rawContent != null ? rawContent : "");
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
        }
        return Optional.of(id);
    }

    @Override
    public boolean update(Connection conn, Long id, String newContent) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, newContent != null ? newContent : "");
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();
            if(affected == 0) {
                logger.warn("Update table content_info had no effect!");
                return false;
            }
        }
        return true;
    }
}
