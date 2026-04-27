package org.example.repository.chunk_retrieval;

import java.util.List;

public interface IChunkRetrievalRepository {
    List<String> retrieveTopChunks(float[] queryEmbedding, int nrChunks);
}
