package org.example.service.file_save;

import org.example.model.file.FileInfo;
import org.example.repository.IFileIndexScanRepository;
import org.example.repository.IRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Class which uses the repositories in order to index files
 */
@Service
public class FileSaver {
    private static final Logger logger = LoggerFactory.getLogger(FileSaver.class);
    private final IFileIndexScanRepository fileIndexScanRepository;
    public FileSaver(IFileIndexScanRepository fileIndexScanRepository) {
        this.fileIndexScanRepository = fileIndexScanRepository;
    }

    /**
     * Method which deletes all files present in DB but not in file system
     * Deletes all the files in the DB with the scan_id < current scan_id
     */
    public void deleteAllFilesNotPresent(IndexingStats stats, Long scanId) {
        int deleted = fileIndexScanRepository.deleteNotSeenInScan(scanId);
        stats.setDeletedCount(deleted);
    }

    /**
     * Method which indexes a file.
     * The file is analyzed and falls within one of three categories:
     *  * 1. NOT INDEXED YET => save
     *  * 2. INDEXED, BUT MODIFIED => update
     *  * 3. INDEXED AND NOT MODIFIED => skip, but update last scan id
     */
    public <E extends FileInfo> void addFile(E fileInfo, IRepository<Long, E> repo, IndexingStats stats, Long scanId) {

        Optional<FileInfo> inDbFileInfo = fileIndexScanRepository.findFileIdByPath(fileInfo);
        inDbFileInfo.ifPresentOrElse(
            inDbInfo -> {
                Long id = inDbInfo.getFileId();
                if(inDbInfo.getMetadata().getLastModificationTime().toMillis() != fileInfo.getMetadata().getLastModificationTime().toMillis()){
                    //INDEXED, BUT MODIFIED
                    repo.update(id, fileInfo);
                    stats.incrementModifiedCount();
                }
                else{
                    //INDEXED AND NOT MODIFIED = SKIPPED
                    fileIndexScanRepository.touchScanId(id, scanId);
                    stats.incrementSkippedCount();
                }
            },
            () -> {
                //NOT INDEXED YET
                repo.save(fileInfo);
                stats.incrementNewCount();
            }
        );


    }
}
