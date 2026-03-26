package org.example.repository;

import org.example.database.IDataSource;
import org.example.model.file.FileInfo;
import org.example.model.file.FileType;
import org.example.model.file.Metadata;
import org.example.repository.persistence.FileInfoPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.attribute.FileTime;

/**
 * READ Repository implementation
 * Support GET_BY_ID, GET_ALL, GET_ENTITY_ID
 */
@Repository
public class FileInfoGetter implements IFileInfoGetter {
    private static final Logger logger = LoggerFactory.getLogger(FileInfoGetter.class);
    private static final String GET_ALL_WITH_METADATA_SQL = """
            SELECT
                fi.file_id AS file_id,
                fi.file_name AS file_name,
                fi.parent_directory_path AS parent_directory_path,
                fi.file_extension AS file_extension,
                fi.file_type AS file_type,
                m.creation_time AS creation_time,
                m.last_modified_time AS last_modified_time,
                m.last_access_time AS last_access_time,
                m.size AS size,
                m.regular_file AS regular_file,
                m.symbolic_link AS symbolic_link,
                m.other_file AS other_file,
                m.file_key AS file_key
            FROM file_info fi
            JOIN metadata m ON fi.file_id = m.file_id
            """;
    private static final String GET_BY_ID_WITH_METADATA_SQL = GET_ALL_WITH_METADATA_SQL + "\nWHERE fi.file_id = ?";
    private final IDataSource dataSource;
    private final FileInfoPersistence fileInfoPersistence;

    public FileInfoGetter(IDataSource dataSource,
                          FileInfoPersistence fileInfoPersistence
                          ) {
        this.dataSource = dataSource;
        this.fileInfoPersistence = fileInfoPersistence;
    }
    @Override
    public Optional<Long> getEntityId(FileInfo entity) {
        try(Connection conn = dataSource.getConnection()){
            return fileInfoPersistence.getEntityId(conn, entity);
        }
        catch (SQLException e){
            logger.error("Failed to retrieve file id for fileName={}, parentDirectoryPath={}", entity.getFileName(), entity.getParentDirectoryPath(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FileInfo> getById(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_BY_ID_WITH_METADATA_SQL)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            FileInfo fileInfo = new FileInfo(
                    rs.getString("file_name"),
                    rs.getString("parent_directory_path"),
                    rs.getString("file_extension"),
                    FileType.valueOf(rs.getString("file_type"))
            );

            Timestamp creation = rs.getTimestamp("creation_time");
            Timestamp lastModified = rs.getTimestamp("last_modified_time");
            Timestamp lastAccess = rs.getTimestamp("last_access_time");
            Metadata metadata = new Metadata(
                    creation == null ? null : FileTime.fromMillis(creation.getTime()),
                    lastModified == null ? null : FileTime.fromMillis(lastModified.getTime()),
                    lastAccess == null ? null : FileTime.fromMillis(lastAccess.getTime()),
                    rs.getLong("size"),
                    rs.getBoolean("regular_file"),
                    rs.getBoolean("symbolic_link"),
                    rs.getBoolean("other_file"),
                    rs.getString("file_key")
            );
            fileInfo.setMetadata(metadata);
            return Optional.of(fileInfo);
        } catch (SQLException e) {
            logger.error("Failed to get file by id={}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<FileInfo>> getAll() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(GET_ALL_WITH_METADATA_SQL)) {
                ResultSet rs = stmt.executeQuery();
                List<FileInfo> results = new ArrayList<>();
                while (rs.next()) {
                    FileInfo fileInfo = new FileInfo(
                            rs.getString("file_name"),
                            rs.getString("parent_directory_path"),
                            rs.getString("file_extension"),
                            FileType.valueOf(rs.getString("file_type"))
                    );

                    Timestamp creation = rs.getTimestamp("creation_time");
                    Timestamp lastModified = rs.getTimestamp("last_modified_time");
                    Timestamp lastAccess = rs.getTimestamp("last_access_time");
                    Metadata metadata = new Metadata(
                            creation == null ? null : FileTime.fromMillis(creation.getTime()),
                            lastModified == null ? null : FileTime.fromMillis(lastModified.getTime()),
                            lastAccess == null ? null : FileTime.fromMillis(lastAccess.getTime()),
                            rs.getLong("size"),
                            rs.getBoolean("regular_file"),
                            rs.getBoolean("symbolic_link"),
                            rs.getBoolean("other_file"),
                            rs.getString("file_key")
                    );
                    fileInfo.setMetadata(metadata);
                    results.add(fileInfo);
                }

                return results.isEmpty() ? Optional.empty() : Optional.of(results);
            }
        } catch (SQLException e) {
            logger.error("Failed to get all files", e);
        }
        return Optional.empty();
    }
}
