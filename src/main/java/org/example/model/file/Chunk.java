package org.example.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Chunk {
    private String filePath;
    private String content;
    private float[] embedding;
}
