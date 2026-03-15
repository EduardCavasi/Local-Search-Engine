package org.example.repository;

import java.util.Optional;

public interface IFileRepository<Id, File>{
    Optional<Long> save(File file);
    boolean delete(Id id);
    Long update(File file);
}
