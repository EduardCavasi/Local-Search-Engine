package org.example.repository;

import org.example.model.FileInfo;

import java.util.List;
import java.util.Optional;

public interface IRepository<Id, E extends FileInfo> {
    Optional<Id> save(E entity);
    void delete(Id id);
    void update(Id id, E entity);
}
