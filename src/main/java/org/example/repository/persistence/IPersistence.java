package org.example.repository.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface IPersistence<Id, Entity> {
    Optional<Id> save(Connection conn, Id id, Entity entity) throws SQLException;
    boolean delete(Connection conn, Id id) throws SQLException;
    boolean update(Connection conn, Id id, Entity entity) throws SQLException;
    Optional<Entity> getById(Connection conn, Id id) throws SQLException;
}
