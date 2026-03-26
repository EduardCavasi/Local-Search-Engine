package org.example.repository;

import org.example.database.IDataSource;
import org.example.model.file.FileInfo;
import org.example.model.file.Metadata;
import org.example.repository.persistence.FileInfoPersistence;
import org.example.repository.persistence.IPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Optional;
import java.util.function.Function;

/**
 * Generic CREATE, UPDATE, DELETE repository implementation
 * E -> an extension of the FileInfo class(TextualFileInfo, MediaFileInfo, PdfFileInfo etc)
 * P -> the type of the extra field that differences the classes extending FileInfo (String for TextualFileInfo)
 * All the operations are implemented as transactions. If one of the parts of an operations fails, the connection rollbacks.
 */
public class FileRepository<E extends FileInfo, P> implements IRepository<Long, E> {

    private final Logger logger = LoggerFactory.getLogger(FileRepository.class);
    /**Datasource*/
    private final IDataSource dataSource;
    /**file_info repository*/
    private final FileInfoPersistence fileInfoPersistence;
    /**metadata repository*/
    private final IPersistence<Long, Metadata> metadataPersistence;
    /**plug in repository for the field that differences the classes extending FileInfo*/
    private final IPersistence<Long, P> plugInPersistence;
    /**getter function for extracting the field that differences the classes extending FileInfo*/
    private final Function<E, P> payloadExtractor;

    public FileRepository(IDataSource dataSource,
                          FileInfoPersistence fileInfoPersistence,
                          IPersistence<Long, Metadata> metadataPersistence,
                          IPersistence<Long, P> plugInPersistence,
                          Function<E, P> payloadExtractor) {
        this.dataSource = dataSource;
        this.fileInfoPersistence = fileInfoPersistence;
        this.metadataPersistence = metadataPersistence;
        this.plugInPersistence = plugInPersistence;
        this.payloadExtractor = payloadExtractor;
    }

    @Override
    public Optional<Long> save(E fileInfo) {
        Optional<Long> fileId = Optional.empty();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                fileId = fileInfoPersistence.save(conn,
                        null,
                        fileInfo);

                if (fileId.isEmpty()) {
                    logger.error("File info insert did not return a generated key for {}", fileInfo.getFileName());
                    conn.rollback();
                    return Optional.empty();
                }

                long id = fileId.get();
                Optional<Long> fileIdMetadata = metadataPersistence.save(conn, id, fileInfo.getMetadata());
                Optional<Long> fileIdPlugIn = plugInPersistence.save(conn, id, payloadExtractor.apply(fileInfo));
                if (fileIdMetadata.isEmpty() || fileIdPlugIn.isEmpty()) {
                    conn.rollback();
                    return Optional.empty();
                }
                //logger.info("File info for file {} saved to database.", fileInfo.getFileName());
                conn.commit();
            }
            catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to save fileName={}, parentDirectoryPath={}, fileType={}", fileInfo.getFileName(), fileInfo.getParentDirectoryPath(), fileInfo.getFileType(), e);
        }
        return fileId;
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean op1Succeed = fileInfoPersistence.delete(conn, id);
                boolean op2Succeed = metadataPersistence.delete(conn, id);
                boolean op3Succeed = plugInPersistence.delete(conn, id);
                if (!(op1Succeed && op2Succeed && op3Succeed)) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            }
            catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            logger.error("Failed to delete file by id={}", id, e);
        }
    }

    @Override
    public void update(Long id, E fileInfo) {
        try(Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            try {
                boolean op1Succeed = fileInfoPersistence.update(conn, id, fileInfo);
                boolean op2Succeed = metadataPersistence.update(conn, id, fileInfo.getMetadata());
                boolean op3Succeed = plugInPersistence.update(conn, id, payloadExtractor.apply(fileInfo));
                if (!(op1Succeed && op2Succeed && op3Succeed)) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            }
            catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            logger.error("Failed to update file by id={} (fileName={}, parentDirectoryPath={}, fileType={})", id, fileInfo.getFileName(), fileInfo.getParentDirectoryPath(), fileInfo.getFileType(), e);
        }
    }
}
