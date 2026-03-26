package org.example.repository;

import org.example.model.file.FileInfo;

import java.util.Optional;

/**
 * Repository interface for common file information
 * Only needed for FileInfo objects(file general info + metadata)
 */
public interface IFileIndexScanRepository {
    Optional<FileInfo> findFileIdByPath(FileInfo entity);
    int deleteNotSeenInScan(Long scanId);
    void touchScanId(Long id, Long scanId);
}
