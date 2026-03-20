package org.example.repository;

import org.example.model.file.FileInfo;

import java.util.Optional;

public interface IRepository<Id, E extends FileInfo> {
    Optional<Id> save(E entity);
    void delete(Id id);
    void update(Id id, E entity);
}
