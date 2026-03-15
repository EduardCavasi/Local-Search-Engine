package org.example.repository;

import org.example.database.DatabaseConnection;
import org.example.model.Metadata;
import org.example.model.TextualFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class TextualFileRepository implements IFileRepository<Long, TextualFileInfo> {
    private final Logger logger = LoggerFactory.getLogger(TextualFileRepository.class);
    @Override
    public Optional<Long> save(TextualFileInfo textualFileInfo) {
        try(Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO file_info (parent_directory_path, file_type, file_extension, file_name) values (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, textualFileInfo.getParentDirectoryPath());
            stmt.setInt(2, 0);
            stmt.setString(3, textualFileInfo.getFileExtension());
            stmt.setString(4, textualFileInfo.getFileName());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long file_id = rs.getLong(1);
                        this.saveMetadata(file_id, textualFileInfo.getMetadata());
                        this.saveFileContent(file_id, textualFileInfo.getContent());
                        logger.info("Textual file info for file {} saved to database.", textualFileInfo.getFileName());
                        return Optional.of(file_id);
                    }
                    else{
                        return Optional.empty();
                    }
                }
            }
            else{
                throw new SQLException("Failed to insert file into the database.");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }
    void saveMetadata(Long file_id, Metadata metadata) {
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "INSERT INTO metadata (file_id, creation_time, file_key, regular_file, symbolic_link, other_file, last_access_time, last_modified_time, size) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, file_id);
            stmt.setTimestamp(2, new Timestamp(metadata.getCreationTime().toMillis()));
            stmt.setString(3, (metadata.getFile_key() == null ? "" : metadata.getFile_key().toString()));
            stmt.setBoolean(4, metadata.getRegularFile());
            stmt.setBoolean(5, metadata.getSymbolic_link());
            stmt.setBoolean(6, metadata.getOther_file());
            stmt.setTimestamp(7, new Timestamp(metadata.getLastAccessTime().toMillis()));
            stmt.setTimestamp(8, new Timestamp(metadata.getLastModificationTime().toMillis()));
            stmt.setLong(9, metadata.getSize());

            stmt.executeUpdate();
        }
        catch(SQLException e) {
            logger.error(e.getMessage());
        }
    }

    void saveFileContent(Long file_id, String content) {
        try(Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO content_info (file_id, raw_content) values (?, ?)");
            stmt.setLong(1, file_id);
            stmt.setString(2, content);
            stmt.executeUpdate();
        }
        catch (SQLException e){
            logger.error(e.getMessage());
        }
    }
    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public Long update(TextualFileInfo textualFileInfo) {
        return 0L;
    }
}
