package org.example.service.file_search;

import org.example.model.file.FileType;
import org.example.model.preview.FilePreview;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Uses the FilePreviewRenderer implementations in order to build the file preview of all files
 * It delegates the building of the preview to the corresponding renderer based on file type.
 */
@Component
public class PreviewBuilder {
    /**maps each file type with the corresponding renderer*/
    private final Map<FileType, FilePreviewRenderer> renderers;

    public PreviewBuilder(List<FilePreviewRenderer> rendererList) {
        this.renderers = rendererList.stream()
                .collect(Collectors.toMap(
                        FilePreviewRenderer::getSupportedType,
                        r -> r,
                        (existing, ignored) -> existing
                ));
    }

    public List<FilePreview> buildPreviews(ResultSet rs) throws SQLException {
        List<FilePreview> previews = new ArrayList<>();
        while (rs.next()) {
            String rawType = rs.getString("file_type");
            if (rawType == null) {
                continue;
            }

            FileType fileType;
            try {
                fileType = FileType.valueOf(rawType);
            } catch (IllegalArgumentException e) {
                continue;
            }

            FilePreviewRenderer renderer = renderers.get(fileType);
            if (renderer != null) {
                previews.add(renderer.buildPreview(rs));
            }
        }
        return previews;
    }
}
