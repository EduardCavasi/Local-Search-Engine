package org.example.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Model class containing general file information and metadata
 */
@AllArgsConstructor
@Getter
@Setter
public class FileInfo {
    private Long fileId;
    private String fileName;
    private String parentDirectoryPath;
    private Metadata metadata;
    private RankInfo rankInfo;
    private String fileExtension;
    private FileType fileType;

    public FileInfo(File file, BasicFileAttributes attr, Long scanId) {
        this.fileName = file.getName();
        this.parentDirectoryPath = file.getParent().replace('\\', '/');
        if (this.fileName.startsWith(".")) {
            this.fileExtension = "";
        }
        else {
            this.fileExtension = this.fileName.substring(this.fileName.lastIndexOf(".") + 1);
        }
        this.metadata = new Metadata(attr, scanId);
        this.rankInfo = new RankInfo(file, attr);
    }

    public FileInfo(Long id, String fileName, String parentDirectoryPath, String fileExtension, FileType fileType) {
        this.fileId = id;
        this.fileName = fileName;
        this.parentDirectoryPath = parentDirectoryPath;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
    }

}
