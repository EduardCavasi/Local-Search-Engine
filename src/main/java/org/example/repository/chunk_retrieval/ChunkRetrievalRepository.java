package org.example.repository.chunk_retrieval;

import org.example.database.IDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ChunkRetrievalRepository implements IChunkRetrievalRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChunkRetrievalRepository.class);
    private static final String RETRIEVAL_SQL =
            "SELECT chunk_info.content AS chunk_content FROM chunk_info"
            + " ORDER BY chunk_info.embedding <=> ?::vector"
            + " LIMIT ?";

    private IDataSource dataSource;

    public ChunkRetrievalRepository(IDataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public List<String> retrieveTopChunks(float[] queryEmbedding, int nrChunks) {
        List<String> chunks = new ArrayList<>();
        try(Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(RETRIEVAL_SQL)) {
            stmt.setString(1, Arrays.toString(queryEmbedding));
            stmt.setInt(2, nrChunks);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chunks.add(rs.getString("chunk_content"));
            }
        } catch (SQLException e) {
            logger.warn("Could not get similar chunks to {}", queryEmbedding, e);
        }
        return chunks;
    }
}
