package org.example.repository.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ContentPersistence {

    private static final String INSERT_SQL =
            "INSERT INTO content_info (file_id, raw_content) VALUES (?, ?)";

    public void save(Connection conn, long fileId, String rawContent) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setLong(1, fileId);
            stmt.setString(2, rawContent != null ? rawContent : "");
            stmt.executeUpdate();
        }
    }
}
