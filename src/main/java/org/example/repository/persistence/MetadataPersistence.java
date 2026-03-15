package org.example.repository.persistence;

import org.example.model.Metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MetadataPersistence {

    private static final String INSERT_SQL =
            "INSERT INTO metadata (file_id, creation_time, file_key, regular_file, symbolic_link, other_file, last_access_time, last_modified_time, size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void save(Connection conn, long fileId, Metadata metadata) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setLong(1, fileId);
            stmt.setTimestamp(2, new Timestamp(metadata.getCreationTime().toMillis()));
            stmt.setString(3, metadata.getFile_key() == null ? "" : metadata.getFile_key().toString());
            stmt.setBoolean(4, metadata.getRegularFile());
            stmt.setBoolean(5, metadata.getSymbolic_link());
            stmt.setBoolean(6, metadata.getOther_file());
            stmt.setTimestamp(7, new Timestamp(metadata.getLastAccessTime().toMillis()));
            stmt.setTimestamp(8, new Timestamp(metadata.getLastModificationTime().toMillis()));
            stmt.setLong(9, metadata.getSize());
            stmt.executeUpdate();
        }
    }
}
