package org.example.config;

import org.apache.tika.Tika;
import org.example.database.IDataSource;
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

@Configuration
public class BeanConfig {

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public IRepository<Long, TextualFileInfo> textualFileRepository(
            IDataSource dataSource,
            FileInfoPersistence fileInfoPersistence,
            MetadataPersistence metadataPersistence,
            ContentPersistence contentPersistence
    ) {
        IPersistence<Long, Metadata> metadataPlugin = metadataPersistence;
        IPersistence<Long, String> contentPlugin = contentPersistence;
        return new FileRepository<>(
                dataSource,
                fileInfoPersistence,
                metadataPlugin,
                contentPlugin,
                TextualFileInfo::getContent
        );
    }
}
