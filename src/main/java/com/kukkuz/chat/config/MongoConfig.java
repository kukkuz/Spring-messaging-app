package com.kukkuz.chat.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.kukkuz.chat.mongo.converter.ChatHistoryWriterConverter;
import com.kukkuz.chat.mongo.event.CascadeSaveMongoEventListener;
import com.kukkuz.chat.mongo.event.ChatHistoryCascadeSaveMongoEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = "com.kukkuz.chat.mongo.repository")
public class MongoConfig extends AbstractMongoConfiguration {

    private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

    @Override
    protected String getDatabaseName() {
        return "chat";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient("127.0.0.1", 27017);
    }

    @Override
    public String getMappingBasePackage() {
        return "com.kukkuz.chat";
    }

    @Override
    public CustomConversions customConversions() {
        converters.add(new ChatHistoryWriterConverter());
        return new CustomConversions(converters);
    }

    @Bean
    public ChatHistoryCascadeSaveMongoEventListener chatHistoryCascadeSaveMongoEventListener() {
        return new ChatHistoryCascadeSaveMongoEventListener();
    }

    @Bean
    public CascadeSaveMongoEventListener cascadingMongoEventListener() {
        return new CascadeSaveMongoEventListener();
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }
}
