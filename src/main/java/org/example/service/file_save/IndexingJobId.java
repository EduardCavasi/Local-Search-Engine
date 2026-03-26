package org.example.service.file_save;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Class taking care of persisting the last scan id
 */
@Component
public class IndexingJobId {
    private static final Logger logger = LoggerFactory.getLogger(IndexingJobId.class);
    private static final File SAVE_FILE = new File("indexing_job_id.json");
    private final ObjectMapper mapper;
    private static final Long INITIAL_SCAN_ID = 0L;

    @Getter
    private Long scanId;

    public IndexingJobId() {
        mapper = new ObjectMapper();
    }
    public IndexingJobId(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void loadState(){
        if(SAVE_FILE.exists()){
            try{
                IndexingJobId indexingJobId = mapper.readValue(SAVE_FILE, IndexingJobId.class);
                this.scanId = indexingJobId.getScanId();
                logger.info("Loaded indexing job id from {}", SAVE_FILE);

            }
            catch (IOException e){
                scanId = INITIAL_SCAN_ID;
                logger.info("Fallback to default indexing job id");
            }
        }
        else{
            scanId = INITIAL_SCAN_ID;
            logger.info("Fallback to default indexing job id");
        }
    }

    @PreDestroy
    public void saveState() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(SAVE_FILE, this);
            logger.info("IndexingJobId saved to {}", SAVE_FILE.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Could not save indexing job id", e);
        }
    }

    public void incrementJobId(){
        scanId++;
    }
}
