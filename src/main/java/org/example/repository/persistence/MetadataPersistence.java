package org.example.repository.persistence;

import org.example.model.file.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Optional;

@Component
public class MetadataPersistence implements IPersistence<Long, Metadata> {
    private static final Logger logger = LoggerFactory.getLogger(MetadataPersistence.class);

    private static final String INSERT_SQL =
            "INSERT INTO metadata (file_id, creation_time, file_key, regular_file, symbolic_link, other_file, last_access_time, last_modified_time, size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM metadata WHERE file_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE metadata SET creation_time = ?, file_key = ?, regular_file = ?, symbolic_link = ?, other_file = ?, last_access_time = ?, last_modified_time = ?, size = ? WHERE file_id = ?";
    @Override
    public Optional<Long> save(Connection conn, Long id, Metadata metadata) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            stmt.setLong(1, id);
            stmt.setObject(2, Timestamp.from(metadata.getCreationTime().toInstant()));
            stmt.setString(3, metadata.getFile_key() == null ? "" : metadata.getFile_key().toString());
            stmt.setBoolean(4, metadata.getRegularFile());
            stmt.setBoolean(5, metadata.getSymbolic_link());
            stmt.setBoolean(6, metadata.getOther_file());
            stmt.setObject(7, Timestamp.from(metadata.getLastAccessTime().toInstant()));
            stmt.setObject(8, Timestamp.from(metadata.getLastModificationTime().toInstant()));
            stmt.setLong(9, metadata.getSize());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
        }
        return Optional.of(id);
    }

    @Override
    public boolean delete(Connection conn, Long id) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)){
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("Delete from table metadata had no effect!");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean update(Connection conn, Long id, Metadata metadata) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)){
            stmt.setObject(1, Timestamp.from(metadata.getCreationTime().toInstant()));
            stmt.setString(2, metadata.getFile_key() == null ? "" : metadata.getFile_key().toString());
            stmt.setBoolean(3, metadata.getRegularFile());
            stmt.setBoolean(4, metadata.getSymbolic_link());
            stmt.setBoolean(5, metadata.getOther_file());
            stmt.setObject(6, Timestamp.from(metadata.getLastAccessTime().toInstant()));
            stmt.setObject(7, Timestamp.from(metadata.getLastModificationTime().toInstant()));
            stmt.setLong(8, metadata.getSize());
            stmt.setLong(9, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("Update table metadata had no effect!");
                return false;
            }
        }
        return true;
    }
}
