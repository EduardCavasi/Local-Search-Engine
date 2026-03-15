package org.example.repository;

import org.example.database.DatabaseConnection;
import org.example.database.IDataSource;
import org.example.model.TextualFileInfo;
import org.example.repository.persistence.ContentPersistence;
import org.example.repository.persistence.FileInfoPersistence;
import org.example.repository.persistence.MetadataPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TextualFileRepository implements IFileRepository<Long, TextualFileInfo> {

    private static final int FILE_TYPE_TEXT = 0;

    private final Logger logger = LoggerFactory.getLogger(TextualFileRepository.class);
    private final IDataSource dataSource;
    private final FileInfoPersistence fileInfoPersistence;
    private final MetadataPersistence metadataPersistence;
    private final ContentPersistence contentPersistence;

    public TextualFileRepository() {
        this(DatabaseConnection.getInstance(), new FileInfoPersistence(), new MetadataPersistence(), new ContentPersistence());
    }

    public TextualFileRepository(IDataSource dataSource,
                                 FileInfoPersistence fileInfoPersistence,
                                 MetadataPersistence metadataPersistence,
                                 ContentPersistence contentPersistence) {
        this.dataSource = dataSource;
        this.fileInfoPersistence = fileInfoPersistence;
        this.metadataPersistence = metadataPersistence;
        this.contentPersistence = contentPersistence;
    }

    @Override
    public Optional<Long> save(TextualFileInfo textualFileInfo) {
        try (Connection conn = dataSource.getConnection()) {
            Optional<Long> fileId = fileInfoPersistence.save(conn,
                    textualFileInfo.getParentDirectoryPath(),
                    FILE_TYPE_TEXT,
                    textualFileInfo.getFileExtension(),
                    textualFileInfo.getFileName());

            if (fileId.isEmpty()) {
                logger.warn("File info insert did not return a generated key for {}", textualFileInfo.getFileName());
                return Optional.empty();
            }

            long id = fileId.get();
            metadataPersistence.save(conn, id, textualFileInfo.getMetadata());
            contentPersistence.save(conn, id, textualFileInfo.getContent());

            logger.info("Textual file info for file {} saved to database.", textualFileInfo.getFileName());
            return Optional.of(id);
        } catch (SQLException e) {
            logger.error("Failed to save textual file: {}", e.getMessage());
            return Optional.empty();
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
