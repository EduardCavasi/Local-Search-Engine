package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public FileInfo(File file, BasicFileAttributes attr) {
        this.fileName = file.getName();
        this.parentDirectoryPath = file.getParent();
        this.fileExtension = this.fileName.substring(this.fileName.lastIndexOf(".") + 1);
        this.metadata = new Metadata(attr);
    }
}
