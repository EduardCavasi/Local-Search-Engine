package org.example.repository.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FileInfoPersistence {

    private static final String INSERT_SQL =
            "INSERT INTO file_info (parent_directory_path, file_type, file_extension, file_name) VALUES (?, ?, ?, ?)";

    public Optional<Long> save(Connection conn, String parentDirectoryPath, int fileType, String fileExtension, String fileName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, parentDirectoryPath);
            stmt.setInt(2, fileType);
            stmt.setString(3, fileExtension);
            stmt.setString(4, fileName);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Optional.of(keys.getLong(1));
                }
            }
        }
        return Optional.empty();
    }
}
