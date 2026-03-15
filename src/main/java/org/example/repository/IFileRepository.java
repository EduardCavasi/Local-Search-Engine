package org.example.repository;

import java.util.Optional;

public interface IFileRepository<Id, E> {
    Optional<Id> save(E entity);
    boolean delete(Id id);
    Id update(E entity);
}
