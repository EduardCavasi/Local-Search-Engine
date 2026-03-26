package org.example.repository;

import org.example.model.file.FileInfo;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for READ operations
 * Only needed for FileInfo objects(file general info + metadata)
 */
public interface IFileInfoGetter {
    Optional<Long> getEntityId(FileInfo entity);
    Optional<FileInfo> getById(Long id);
    Optional<List<FileInfo>> getAll();
}
