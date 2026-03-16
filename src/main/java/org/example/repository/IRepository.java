package org.example.repository;

import java.util.Optional;

public interface IRepository<Id, E> {
    Optional<Id> save(E entity);
    void delete(Id id);
    void update(Id id, E entity);
}
