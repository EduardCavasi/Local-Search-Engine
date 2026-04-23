package org.example.repository.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository interface for CREATE, UPDATE, DELETE
 */
public interface IPersistence<Id, Entity> {
    Optional<Id> save(Connection conn, Id id, Entity entity) throws SQLException;
    boolean update(Connection conn, Id id, Entity entity) throws SQLException;
}
