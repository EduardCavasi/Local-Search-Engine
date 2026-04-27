package org.example.config;

import org.apache.tika.Tika;
import org.example.database.IDataSource;
import org.example.model.file.*;
import org.example.repository.FileRepository;
import org.example.repository.IRepository;
import org.example.repository.persistence.IPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config class for tika bean and textualFileRepository bean
 */
@Configuration
public class BeanConfig {

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public IRepository<Long, TextualFileInfo> textualFileRepository(
            IDataSource dataSource,
            IPersistence<Long, FileInfo> fileInfoPersistence,
            IPersistence<Long, Metadata> metadataPersistence,
            IPersistence<Long, RankInfo> rankInfoPersistence,
            IPersistence<Long, TextualPayload> textualPayloadPersistence
    ) {

        return new FileRepository<>(
                dataSource,
                fileInfoPersistence,
                metadataPersistence,
                rankInfoPersistence,
                textualPayloadPersistence,
                TextualFileInfo::getTextualPayload
        );
    }
}
