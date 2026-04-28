package org.example.repository.chunk_retrieval;

import org.example.database.IDataSource;
import org.postgresql.util.PGobject;
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
            "SELECT chunk_info.content AS chunk_content, chunk_info.embedding <=> ?::vector as distance FROM chunk_info"
            + " ORDER BY distance"
            + " LIMIT ?";

    private final IDataSource dataSource;

    public ChunkRetrievalRepository(IDataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public List<String> retrieveTopChunks(float[] queryEmbedding, int nrChunks) {
        logger.info("Retrieving top similar chunks with user query!");
        List<String> chunks = new ArrayList<>();
        try(Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(RETRIEVAL_SQL)) {
            String vectorStr = Arrays.toString(queryEmbedding).replace(" ", "");
            PGobject pgVector = new PGobject();
            pgVector.setType("vector");
            pgVector.setValue(vectorStr);

            stmt.setObject(1, pgVector);
            stmt.setInt(2, nrChunks);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chunks.add(rs.getString("chunk_content"));
                logger.info("Chunk: {}", rs.getString("chunk_content"));
                logger.info("Distance: {}", rs.getFloat("distance"));
            }
        } catch (SQLException e) {
            logger.warn("Could not get similar chunks to {}", queryEmbedding, e);
        }
        return chunks;
    }
}
