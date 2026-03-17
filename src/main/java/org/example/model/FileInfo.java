package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;

@AllArgsConstructor
@Getter
@Setter
public class FileInfo {
    private String fileName;
    private String parentDirectoryPath;
    private Metadata metadata;
    private String fileExtension;
    private FileType fileType;

    public FileInfo(File file, BasicFileAttributes attr) {
        this.fileName = file.getName();
        this.parentDirectoryPath = file.getParent();
        this.fileExtension = this.fileName.substring(this.fileName.lastIndexOf(".") + 1);
        this.metadata = new Metadata(attr);
    }

    public FileInfo(String fileName, String parentDirectoryPath, String fileExtension, FileType fileType) {
        this.fileName = fileName;
        this.parentDirectoryPath = parentDirectoryPath;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
    }
}
