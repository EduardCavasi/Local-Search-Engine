package org.example.service.file_search;

import org.example.model.file.FileType;
import org.example.model.preview.FilePreview;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for building FilePreviews out of ResultSet objects
 */
public interface FilePreviewRenderer {
    FileType getSupportedType();

    FilePreview buildPreview(ResultSet rs) throws SQLException;
}

