package org.example.repository.persistence;

import org.example.model.file.TextualPayload;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class TextualPayloadPersistence implements IPersistence<Long, TextualPayload> {

    private final ContentPersistence contentPersistence;
    private final ChunkPersistence chunkPersistence;

    public TextualPayloadPersistence(ContentPersistence contentPersistence, ChunkPersistence chunkPersistence) {
        this.contentPersistence = contentPersistence;
        this.chunkPersistence = chunkPersistence;
    }

    @Override
    public Optional<Long> save(Connection conn, Long id, TextualPayload textualPayload) throws SQLException {
        Optional<Long> contentSaveId = contentPersistence.save(conn, id, textualPayload.getContent());
        Optional<Long> chunkSaveId = chunkPersistence.save(conn, id, textualPayload.getChunks());
        if(contentSaveId.isPresent() && chunkSaveId.isPresent()) {
            return contentSaveId;
        }
        return Optional.empty();
    }

    @Override
    public boolean update(Connection conn, Long id, TextualPayload textualPayload) throws SQLException {
        boolean contentUpdateId = contentPersistence.update(conn, id, textualPayload.getContent());
        boolean chunkUpdateId = chunkPersistence.update(conn, id, textualPayload.getChunks());
        return contentUpdateId && chunkUpdateId;
    }
}
