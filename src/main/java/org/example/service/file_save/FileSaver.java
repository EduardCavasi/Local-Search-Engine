package org.example.service.file_save;

import org.example.model.file.FileInfo;
import org.example.model.file.FileType;
import org.example.repository.IFileInfoGetter;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FileSaver {
    private static final Logger logger = LoggerFactory.getLogger(FileSaver.class);
    private final IFileInfoGetter fileInfoGetter;
    public FileSaver(IFileInfoGetter fileInfoGetter){
        this.fileInfoGetter = fileInfoGetter;
    }
    public void deleteAllFilesNotPresent(Map<FileType, IRepository<Long, ? extends FileInfo>> repo, IndexingStats stats) {
        Optional<List<FileInfo>> fileInfos = fileInfoGetter.getAll();

        fileInfos.ifPresent(infos -> infos.forEach(info -> {
            String fullPath = info.getParentDirectoryPath() + '/' + info.getFileName();
            File file = new File(fullPath);
            if(!file.exists()){
                Optional<Long> id = fileInfoGetter.getEntityId(info);
                id.ifPresent(repo.get(info.getFileType())::delete);
                stats.incrementDeletedCount();
            }
        }));
    }
    public <E extends FileInfo> void addFile(E fileInfo, IRepository<Long, E> repo, IndexingStats stats) {

        Optional<Long> fileId = fileInfoGetter.getEntityId(fileInfo);
        fileId.ifPresentOrElse(
            id -> {
               Optional<FileInfo> inDbFileInfo = fileInfoGetter.getById(id);
               inDbFileInfo.ifPresent(inDbInfo -> {
                   if(inDbInfo.getMetadata().getLastModificationTime().toMillis() != fileInfo.getMetadata().getLastModificationTime().toMillis()){
                       repo.update(id, fileInfo);
                       stats.incrementModifiedCount();
                   }
                   else{
                       stats.incrementSkippedCount();
                   }
               });
            },
            () -> {
                repo.save(fileInfo);
                stats.incrementNewCount();
            }
        );


    }
}
