package org.example.repository;

import org.example.database.IDataSource;
import org.example.model.file.FileInfo;

import org.example.model.file.FileType;
import org.example.model.file.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.util.Optional;

/**
 * General Repository implementation(for FileInfo objects)
 * Supports GET_BY_PATH, BUNCH_DELETE, UPDATE_SCAN_ID
 */
@Repository
public class FileIndexScanRepository implements IFileIndexScanRepository {
    private static final Logger logger = LoggerFactory.getLogger(FileIndexScanRepository.class);
    private static final String GET_BY = """
            SELECT
                fi.file_id AS file_id,
                fi.file_name AS file_name,
                fi.parent_directory_path AS parent_directory_path,
                fi.file_extension AS file_extension,
                fi.file_type AS file_type,
                m.creation_time AS creation_time,
                m.last_modified_time AS last_modified_time,
                m.size AS size,
                m.regular_file AS regular_file,
                m.symbolic_link AS symbolic_link,
                m.other_file AS other_file,
                m.file_key AS file_key,
                m.scan_id AS scan_id
            FROM file_info fi
            JOIN metadata m ON fi.file_id = m.file_id
            """;
    private static final String GET_BY_PATH = GET_BY + "\nWHERE fi.parent_directory_path = ?\n" + "\nAND fi.file_name = ?\n";
    private static final String BUNCH_DELETE = """
            DELETE FROM file_info fi
                WHERE EXISTS (
                    SELECT 1
                    FROM metadata m
                    WHERE m.file_id = fi.file_id
                    AND m.scan_id < ?
                );
            """;
    private static final String UPDATE_SCAN_ID_ONLY_SQL = """
            UPDATE metadata
            SET scan_id = ?
            WHERE file_id = ?;
            """;
    private final IDataSource dataSource;

    public FileIndexScanRepository(IDataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public Optional<FileInfo> findFileIdByPath(FileInfo entity) {
        try(Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(GET_BY_PATH)){
            stmt.setString(1, entity.getParentDirectoryPath());
            stmt.setString(2, entity.getFileName());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            FileInfo fileInfo = new FileInfo(
                    rs.getLong("file_id"),
                    rs.getString("file_name"),
                    rs.getString("parent_directory_path"),
                    rs.getString("file_extension"),
                    FileType.valueOf(rs.getString("file_type"))
            );

            Timestamp creation = rs.getTimestamp("creation_time");
            Timestamp lastModified = rs.getTimestamp("last_modified_time");
            Metadata metadata = new Metadata(
                    creation == null ? null : FileTime.fromMillis(creation.getTime()),
                    lastModified == null ? null : FileTime.fromMillis(lastModified.getTime()),
                    rs.getLong("size"),
                    rs.getBoolean("regular_file"),
                    rs.getBoolean("symbolic_link"),
                    rs.getBoolean("other_file"),
                    rs.getString("file_key"),
                    rs.getLong("scan_id")
            );
            fileInfo.setMetadata(metadata);
            return Optional.of(fileInfo);
        }
        catch (SQLException e){
            logger.error("Failed to retrieve file id for fileName={}, parentDirectoryPath={}", entity.getFileName(), entity.getParentDirectoryPath(), e);
        }
        return Optional.empty();
    }

    @Override
    public int deleteNotSeenInScan(Long scanId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(BUNCH_DELETE)) {
            stmt.setLong(1, scanId);
            return stmt.executeUpdate();
        }
        catch (SQLException e){
            logger.error("Failed to bunch delete scanId={}", scanId, e);
        }
        return 0;
    }

    @Override
    public void touchScanId(Long id, Long scanId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SCAN_ID_ONLY_SQL)) {
            stmt.setLong(1, scanId);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        catch (SQLException e){
            logger.error("Update scan id for file if {}", id, e);
        }
    }
}
