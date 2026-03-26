package org.example.repository.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository interface for retrieving entity ID from database
 */
public interface IFileInfoRetrieval<Id, E>{
    Optional<Id> getEntityId(Connection conn, E entity) throws SQLException;
}
