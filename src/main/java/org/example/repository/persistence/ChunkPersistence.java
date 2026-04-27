package org.example.repository.persistence;

import org.example.model.file.Chunk;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ChunkPersistence implements IPersistence<Long, List<Chunk>> {
    private static final String INSERT_SQL =
            "INSERT INTO chunk_info (file_id, file_path, content, embedding) VALUES (?, ?, ?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM chunk_info WHERE file_id = ?";
    @Override
    public Optional<Long> save(Connection conn, Long id, List<Chunk> chunks) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            for(Chunk chunk : chunks) {
                stmt.setLong(1, id);
                stmt.setString(2, chunk.getFilePath());
                stmt.setString(3, chunk.getContent());
                stmt.setObject(4, chunk.getEmbedding());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        return Optional.of(id);
    }

    @Override
    public boolean update(Connection conn, Long id, List<Chunk> chunks) throws SQLException {
        try (PreparedStatement deleteStmt = conn.prepareStatement(DELETE_SQL)) {
            deleteStmt.setLong(1, id);
            deleteStmt.executeUpdate();
            save(conn, id, chunks);
        }
        return true;
    }
}
