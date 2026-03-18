package org.example.service.file_save;

import org.example.model.FileInfo;
import org.example.model.FileType;
import org.example.repository.FileInfoGetter;
import org.example.repository.IFileInfoGetter;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSaver {
    private static final Logger logger = LoggerFactory.getLogger(FileSaver.class);
    private final IFileInfoGetter fileInfoGetter;
    public FileSaver(){
        fileInfoGetter = new FileInfoGetter();
    }
    public void deleteAllFilesNotPresent(Map<FileType, IRepository<Long, ? extends FileInfo>> repo, FileCrawlerStats stats) {
        Optional<List<FileInfo>> fileInfos = fileInfoGetter.getAll();

        fileInfos.ifPresent(infos -> infos.forEach(info -> {
            String fullPath = info.getParentDirectoryPath() + File.separator + info.getFileName();
            File file = new File(fullPath);
            if(!file.exists()){
                Optional<Long> id = fileInfoGetter.getEntityId(info);
                id.ifPresent(repo.get(info.getFileType())::delete);
                stats.getDeletedCount().incrementAndGet();
            }
        }));
    }
    public <E extends FileInfo> void addFile(E fileInfo, IRepository<Long, E> repo, FileCrawlerStats stats) {

        Optional<Long> fileId = fileInfoGetter.getEntityId(fileInfo);
        fileId.ifPresentOrElse(
            id -> {
               Optional<FileInfo> inDbFileInfo = fileInfoGetter.getById(id);
               inDbFileInfo.ifPresent(inDbInfo -> {
                   if(inDbInfo.getMetadata().getLastModificationTime().toMillis() != fileInfo.getMetadata().getLastModificationTime().toMillis()){
                       repo.update(id, fileInfo);
                       stats.getModifiedCount().incrementAndGet();
                   }
                   else{
                       stats.getSkippedCount().incrementAndGet();
                   }
               });
            },
            () -> {
                repo.save(fileInfo);
                stats.getNewCount().incrementAndGet();
            }
        );


    }
}
