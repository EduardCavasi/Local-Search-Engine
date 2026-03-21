package org.example.service.file_search;


import lombok.NoArgsConstructor;
import org.example.model.file.FileType;
import org.example.model.preview.FilePreview;
import org.example.model.preview.TextualFilePreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public class PreviewBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PreviewBuilder.class);
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

        if(contentSnippet.isEmpty()){
            contentSnippet = getTextPreview(filePath, 30);
        }

        return new TextualFilePreview(
                fileName != null ? fileName : "",
                filePath,
                contentSnippet
        );
    }

    public String getTextPreview(String filePath, int wordLimit) {
        StringBuilder result = new StringBuilder();

        try (Reader reader = new BufferedReader(new FileReader(filePath))) {
            int ch;
            int wordCount = 0;
            boolean inWord = false;

            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                result.append(c);

                if (Character.isWhitespace(c)) {
                    if (inWord) {
                        wordCount++;
                        inWord = false;

                        if (wordCount >= wordLimit) {
                            break;
                        }
                    }
                } else {
                    inWord = true;
                }
            }

            if (inWord && wordCount < wordLimit) {
                wordCount++;
            }
        }
        catch (IOException ex) {
            logger.warn("Unable to read file: {}", filePath);
        }

        return result.toString();
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
