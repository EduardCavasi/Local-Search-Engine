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
import java.util.ArrayList;
import java.util.List;

@Component
public class Tokenizer {
    private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);
    private static final Integer MIN_CHUNK_SIZE = 150;
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
            content = Files.readString(filePath);
            String[] sentences = content.split("[.!?;:\n]");
            int cur_chunk_size = 0;
            StringBuilder cur_sentence = new StringBuilder();
            for (String sentence : sentences) {
                cur_chunk_size += sentence.split("\\s+").length;
                cur_sentence.append(sentence).append(".");
                if(cur_chunk_size > MIN_CHUNK_SIZE) {
                    chunks.add(new Chunk(filePath.toString(), cur_sentence.toString(), getEmbedding(cur_sentence.toString())));
                    cur_chunk_size = 0;
                    cur_sentence = new StringBuilder();
                }
            }
            if(cur_chunk_size < MIN_CHUNK_SIZE && !cur_sentence.isEmpty()) {
                chunks.add(new Chunk(filePath.toString(), cur_sentence.toString(), getEmbedding(cur_sentence.toString())));
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
