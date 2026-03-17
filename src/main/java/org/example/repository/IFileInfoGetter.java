package org.example.repository;

import org.example.model.FileInfo;

import java.util.List;
import java.util.Optional;

public interface IFileInfoGetter {
    Optional<Long> getEntityId(FileInfo entity);
    Optional<FileInfo> getById(Long id);
    Optional<List<FileInfo>> getAll();
}
