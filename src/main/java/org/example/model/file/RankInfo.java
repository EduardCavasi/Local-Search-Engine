package org.example.model.file;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;

@Getter
@Setter
public class RankInfo {
    private String filePath;
    private Integer depthPath;
    private Integer lengthPath;
    private Integer extensionPriority;
    private Long size;
    private FileTime lastModified;
    private Double combinedScore;
    private static final Map<String, Integer> defaultExtensionPriority = Map.ofEntries(
            Map.entry("java", 11),
            Map.entry("pdf", 10),
            Map.entry("doc", 9),
            Map.entry("docx", 9),
            Map.entry("txt", 6),
            Map.entry("md", 7),
            Map.entry("csv", 5),
            Map.entry("json", 6),
            Map.entry("xml", 6),
            Map.entry("jpg", 3),
            Map.entry("png", 3),
            Map.entry("mp4", 2),
            Map.entry("zip", 1)
    );
    public RankInfo(File file, BasicFileAttributes attr) {
        this.filePath = file.getAbsolutePath().replace("\\", "/");
        this.depthPath = calculateDepth(this.filePath);
        this.lengthPath = calculateLength(this.filePath);
        this.extensionPriority = calculateExtensionPriority(this.filePath);
        this.size = attr.size();
        this.lastModified = attr.lastModifiedTime();
        this.combinedScore = calculateCombinedScore();
    }

    private Integer calculateDepth(String filePath) {
        return filePath.split("/").length - 1;
    }
    private Integer calculateLength(String filePath) {
        return filePath.length();
    }
    private Integer calculateExtensionPriority(String filePath) {
        return defaultExtensionPriority.getOrDefault(filePath.substring(filePath.lastIndexOf(".") + 1), 0);
    }
    private Double calculateCombinedScore() {
        double score = 0.0;

        // Extension priority (0–10)
        double extScore = (extensionPriority != null ? extensionPriority : 0) / 10.0;

        // Depth (shallower is better)
        double depthScore = 1.0 / (depthPath + 1);

        // Path length (shorter is better)
        double lengthScore = 1.0 / (lengthPath + 1);

        // Size (smaller is slightly better, log scale)
        double sizeScore = 1.0 / Math.log(size + 10);

        // Recency (newer is better)
        long now = System.currentTimeMillis();
        long lastModMillis = lastModified.toMillis();
        long days = (now - lastModMillis) / (1000 * 60 * 60 * 24);

        double recencyScore;
        if (days < 7) recencyScore = 1.0;
        else if (days < 30) recencyScore = 0.7;
        else if (days < 180) recencyScore = 0.4;
        else recencyScore = 0.1;

        // Combine with weights
        score =
                extScore * 0.35 +
                        depthScore * 0.20 +
                        lengthScore * 0.15 +
                        sizeScore * 0.10 +
                        recencyScore * 0.20;

        return score;
    }
}
