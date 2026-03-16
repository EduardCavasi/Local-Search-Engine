package org.example.service.file_search;


import lombok.NoArgsConstructor;
import org.example.model.FileType;
import org.example.model.preview.FilePreview;
import org.example.model.preview.TextualFilePreview;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class PreviewBuilder {

    public List<FilePreview> buildPreviews(ResultSet rs) throws SQLException {
        List<FilePreview> previews = new ArrayList<>();
        while (rs.next()) {
            FileType fileType = FileType.valueOf(rs.getString("file_type"));
            switch (fileType) {
                case TEXTUAL_FILE -> previews.add(buildTextualPreview(rs));
            }
        }
        return previews;
    }

    private TextualFilePreview buildTextualPreview(ResultSet rs) {
        String fileName = getString(rs, "file_name");
        String parentPath = getString(rs, "parent_directory_path");
        String contentSnippet = getString(rs, "preview_content");

        contentSnippet = stripHeadlineMarkup(contentSnippet);


        String filePath = (parentPath != null && fileName != null)
                ? parentPath + File.separator + fileName
                : (fileName != null ? fileName : "");

        return new TextualFilePreview(
                fileName != null ? fileName : "",
                filePath,
                contentSnippet != null ? contentSnippet : ""
        );
    }

    private static String stripHeadlineMarkup(String headline) {
        if (headline == null) return "";
        return headline.replaceAll("<[^>]+>", "").replaceAll("&([^;]+);", " ").trim();
    }

    private static String getString(ResultSet rs, String columnLabel) {
        try {
            return rs.getString(columnLabel);
        } catch (SQLException e) {
            return null;
        }
    }
}
