package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@Getter
@Setter
public class Metadata {
    private FileTime creationTime;
    private FileTime lastModificationTime;
    private FileTime lastAccessTime;
    private Long size;
    private Boolean regularFile;
    private Boolean symbolic_link;
    private Boolean other_file;
    private Object file_key;

    public Metadata(BasicFileAttributes attr) {
        this.creationTime = attr.creationTime();
        this.lastModificationTime = attr.lastModifiedTime();
        this.lastAccessTime = attr.lastAccessTime();
        this.size = attr.size();
        this.regularFile = attr.isRegularFile();
        this.symbolic_link = attr.isSymbolicLink();
        this.other_file = attr.isOther();
        this.file_key = attr.fileKey();
    }
}
