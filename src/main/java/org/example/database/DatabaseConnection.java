package org.example.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.error.DatabaseException;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection implements IDataSource {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    static {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("db.properties");
            props.load(fis);
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.pass"));
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new DatabaseException("Failed to initialize database connection pool", e);
        }
    }

    private DatabaseConnection() {}

    public static IDataSource getInstance() {
        return INSTANCE;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
