package org.example.config;

import org.apache.tika.Tika;
import org.example.database.IDataSource;
import org.example.model.file.FileInfo;
import org.example.model.file.Metadata;
import org.example.model.file.TextualFileInfo;
import org.example.repository.FileRepository;
import org.example.repository.IRepository;
import org.example.repository.persistence.ContentPersistence;
import org.example.repository.persistence.FileInfoPersistence;
import org.example.repository.persistence.IPersistence;
import org.example.repository.persistence.MetadataPersistence;
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
            IPersistence<Long, String> contentPersistence
    ) {

        return new FileRepository<>(
                dataSource,
                fileInfoPersistence,
                metadataPersistence,
                contentPersistence,
                TextualFileInfo::getContent
        );
    }
}
