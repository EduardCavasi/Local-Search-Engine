package org.example.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * Model class containing metadata extracted from BasicFileAttributes object corresponding to a file
 */
@AllArgsConstructor
@Getter
@Setter
public class Metadata {
    private FileTime creationTime;
    private FileTime lastModificationTime;
    private Long size;
    private Boolean regularFile;
    private Boolean symbolic_link;
    private Boolean other_file;
    private Object file_key;
    private Long scanId;

    public Metadata(BasicFileAttributes attr, Long scanId) {
        this.creationTime = attr.creationTime();
        this.lastModificationTime = attr.lastModifiedTime();
        this.size = attr.size();
        this.regularFile = attr.isRegularFile();
        this.symbolic_link = attr.isSymbolicLink();
        this.other_file = attr.isOther();
        this.file_key = attr.fileKey();
        this.scanId = scanId;
    }
}
