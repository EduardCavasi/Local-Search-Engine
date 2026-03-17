package org.example.repository;

import org.example.database.DatabaseConnection;
import org.example.database.IDataSource;
import org.example.model.FileInfo;
import org.example.model.Metadata;
import org.example.repository.persistence.FileInfoPersistence;
import org.example.repository.persistence.IPersistence;
import org.example.repository.persistence.MetadataPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileInfoGetter implements IFileInfoGetter {
    private static final Logger logger = LoggerFactory.getLogger(FileInfoGetter.class);
    private final IDataSource dataSource;
    private final FileInfoPersistence fileInfoPersistence;
    private final IPersistence<Long, Metadata> metadataPersistence;

    public FileInfoGetter(IDataSource dataSource,
                          FileInfoPersistence fileInfoPersistence,
                          IPersistence<Long, Metadata> metadataPersistence
                          ) {
        this.dataSource = dataSource;
        this.fileInfoPersistence = fileInfoPersistence;
        this.metadataPersistence = metadataPersistence;
    }
    public FileInfoGetter(){
        this(DatabaseConnection.getInstance(), new FileInfoPersistence(), new MetadataPersistence());
    }
    @Override
    public Optional<Long> getEntityId(FileInfo entity) {
        try(Connection conn = dataSource.getConnection()){
            return fileInfoPersistence.getEntityId(conn, entity);
        }
        catch (SQLException e){
            logger.error("Failed to retrieve file info {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileInfo> getById(Long id) {
        try(Connection conn = dataSource.getConnection()){
            Optional<FileInfo> fileInfoOptional = fileInfoPersistence.getById(conn, id);
            Optional<Metadata> metadataOptional = metadataPersistence.getById(conn, id);
            if(fileInfoOptional.isEmpty() || metadataOptional.isEmpty()) {
                return Optional.empty();
            }
            FileInfo fileInfo = fileInfoOptional.get();
            Metadata metadata = metadataOptional.get();
            fileInfo.setMetadata(metadata);
            return Optional.of(fileInfo);
        }
        catch (SQLException e){
            logger.error("Failed to get file by id: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<FileInfo>> getAll() {
        try (Connection conn = dataSource.getConnection()) {
            Optional<List<FileInfo>> fileInfosOptional = fileInfoPersistence.getAll(conn);
            if (fileInfosOptional.isEmpty()) {
                return Optional.empty();
            }

            List<FileInfo> fileInfos = fileInfosOptional.get();
            List<FileInfo> complete = new ArrayList<>(fileInfos.size());
            for (FileInfo fileInfo : fileInfos) {
                Optional<Long> idOptional = fileInfoPersistence.getEntityId(conn, fileInfo);
                if (idOptional.isEmpty()) {
                    logger.warn("Skipping file_info row without resolvable id for file {}", fileInfo.getFileName());
                    continue;
                }

                long id = idOptional.get();
                Optional<Metadata> metadataOptional = metadataPersistence.getById(conn, id);
                if (metadataOptional.isEmpty()) {
                    logger.warn("Skipping file_info id {} because metadata missing", id);
                    continue;
                }

                fileInfo.setMetadata(metadataOptional.get());
                complete.add(fileInfo);
            }

            return complete.isEmpty() ? Optional.empty() : Optional.of(complete);
        } catch (SQLException e) {
            logger.error("Failed to get all files: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
