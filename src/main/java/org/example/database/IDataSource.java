package org.example.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for database connection
*/
public interface IDataSource {
    /**
     * return a Connection object to the datasource
     */
    Connection getConnection() throws SQLException;
}