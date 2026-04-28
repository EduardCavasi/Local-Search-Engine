package org.example.service.file_save;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import org.example.model.file.Chunk;
import org.example.model.file.TextualPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class Tokenizer {
    private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);
    private static final Integer MIN_CHUNK_SIZE = 500;
    private static final Integer CHUNK_OVERLAP = 50;
    private static final Integer MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final Predictor<String, float[]> predictor;
    public Tokenizer() throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<String, float[]> criteria =
                Criteria.builder()
                        .setTypes(String.class, float[].class)
                        .optApplication(Application.NLP.TEXT_EMBEDDING)
                        .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                        .optEngine("PyTorch")
                        .build();
        ZooModel<String, float[]> model = criteria.loadModel();
        this.predictor = model.newPredictor();
    }

    public TextualPayload tokenize(Path filePath) {
        List<Chunk> chunks = new ArrayList<>();
        String content = "";
        try {
            if (Files.size(filePath) > MAX_FILE_SIZE) {
                return new TextualPayload("", List.of());
            }
            content = Files.readString(filePath);

            String[] sentences = content.split("(?<=[.!?;:\\n])\\s+|\\n{2,}");

            int wordCount = 0;
            StringBuilder chunk = new StringBuilder();
            Deque<String> overlapBuffer = new ArrayDeque<>();

            for (String sentence : sentences) {
                String trimmed = sentence.trim();
                if (trimmed.isEmpty()) continue;

                int sentenceWords = trimmed.split("\\s+").length;
                wordCount += sentenceWords;
                chunk.append(trimmed).append(" ");

                overlapBuffer.addLast(trimmed);

                if (wordCount >= MIN_CHUNK_SIZE) {
                    String chunkText = chunk.toString().trim();
                    chunks.add(new Chunk(
                            filePath.toString(),
                            chunkText,
                            getEmbedding(chunkText)
                    ));

                    chunk = new StringBuilder();
                    wordCount = 0;

                    Deque<String> nextBuffer = new ArrayDeque<>();
                    int overlapWords = 0;

                    String[] buffered = overlapBuffer.toArray(new String[0]);
                    for (int i = buffered.length - 1; i >= 0 && overlapWords < CHUNK_OVERLAP; i--) {
                        int w = buffered[i].split("\\s+").length;
                        if (overlapWords + w <= CHUNK_OVERLAP) {
                            nextBuffer.addFirst(buffered[i]);
                            overlapWords += w;
                        }
                    }

                    for (String s : nextBuffer) {
                        chunk.append(s).append(" ");
                        wordCount += s.split("\\s+").length;
                    }
                    overlapBuffer = nextBuffer;
                }
            }

            if (chunk.length() > 10) {
                String chunkText = chunk.toString().trim();
                chunks.add(new Chunk(
                        filePath.toString(),
                        chunkText,
                        getEmbedding(chunkText)
                ));
            }

        } catch (IOException | TranslateException e) {
            logger.warn("Could not tokenize file {}!", filePath, e);
        }
        return new TextualPayload(content, chunks);
    }

    public float[] getEmbedding(String sentence) throws TranslateException {
        return predictor.predict(sentence);
    }
}
