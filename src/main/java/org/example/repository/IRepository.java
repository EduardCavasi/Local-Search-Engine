package org.example.repository;

import org.example.model.file.FileInfo;

import java.util.Optional;

/**
 * General repository interface for CREATE, UPDATE, DELETE any type of file
 */
public interface IRepository<Id, E extends FileInfo> {
    Optional<Id> save(E entity);
    void update(Id id, E entity);
}
