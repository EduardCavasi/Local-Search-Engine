package org.example.repository.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContentPersistence implements IPersistence<Long, String> {
    private static final Logger logger = LoggerFactory.getLogger(ContentPersistence.class);
    private static final String INSERT_SQL =
            "INSERT INTO content_info (file_id, raw_content) VALUES (?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM content_info WHERE file_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE content_info SET raw_content = ? WHERE file_id = ?";
    private static final String GET_BY_ID_SQL =
            "SELECT * FROM content_info WHERE file_id = ?";
    private static final String GET_ALL_SQL =
            "SELECT * FROM content_info";
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
    public boolean delete(Connection conn, Long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            if(affected == 0) {
                logger.warn("Delete from table content_info had no effect!");
                return false;
            }
        }
        return true;
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

    @Override
    public Optional<String> getById(Connection conn, Long id) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(GET_BY_ID_SQL)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                String content = rs.getString("raw_content");
                return Optional.of(content);
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<String>> getAll(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(GET_ALL_SQL)) {
            ResultSet rs = stmt.executeQuery();
            List<String> contents = new ArrayList<>();
            while (rs.next()) {
                contents.add(rs.getString("raw_content"));
            }
            return contents.isEmpty() ? Optional.empty() : Optional.of(contents);
        }
    }
}
