package org.example.repository.persistence;

import org.example.model.file.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FileInfoPersistence implements IPersistence<Long, FileInfo>, IFileInfoRetrieval<Long, FileInfo> {
    private static final Logger logger = LoggerFactory.getLogger(FileInfoPersistence.class);
    private static final String INSERT_SQL =
            "INSERT INTO file_info (parent_directory_path, file_type, file_extension, file_name) VALUES (?, ?, ?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM file_info WHERE file_info.file_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE file_info SET parent_directory_path = ?, file_type = ?, file_extension = ?, file_name = ? WHERE file_info.file_id = ?";
    private static final String RETRIEVAL_SQL =
            "SELECT file_id from file_info WHERE file_info.parent_directory_path = ? AND file_name = ?";
    @Override
    public Optional<Long> save(Connection conn, Long id,  FileInfo fileInfo) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fileInfo.getParentDirectoryPath());
            stmt.setString(2, fileInfo.getFileType().name());
            stmt.setString(3, fileInfo.getFileExtension());
            stmt.setString(4, fileInfo.getFileName());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Optional.of(keys.getLong(1));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean delete(Connection conn, Long id) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)){
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("Delete from table file_info had no effect!");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean update(Connection conn, Long id, FileInfo fileInfo) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)){
            stmt.setString(1, fileInfo.getParentDirectoryPath());
            stmt.setString(2, fileInfo.getFileType().name());
            stmt.setString(3, fileInfo.getFileExtension());
            stmt.setString(4, fileInfo.getFileName());
            stmt.setLong(5, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                logger.warn("Update table file_info had no effect!");
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Long> getEntityId(Connection conn, FileInfo entity) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement(RETRIEVAL_SQL)){
            stmt.setString(1, entity.getParentDirectoryPath());
            stmt.setString(2, entity.getFileName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getLong(1));
            }
            return Optional.empty();
        }
    }
}
