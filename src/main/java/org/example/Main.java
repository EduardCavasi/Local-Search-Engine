package org.example;

import org.example.model.TextualFileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {
    public static void main(String[] args) throws IOException {
        Path filePath = Path.of("src/main/java/org/example/model/FileInfo.java");

        BasicFileAttributes attrs =
                Files.readAttributes(filePath, BasicFileAttributes.class);

        File file = filePath.toFile();
        TextualFileInfo info = new TextualFileInfo(file, attrs);
        System.out.println(info.getFileName());
        System.out.println(info.getFileExtension());
        System.out.println(info.getParentDirectoryPath());
        System.out.println(info.getContent());
        System.out.println(info.getMetadata().getSize());
    }
}