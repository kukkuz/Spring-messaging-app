package com.kukkuz.chat.mongo.event;

import com.kukkuz.chat.mongo.model.ChatHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

public class ChatHistoryCascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {
    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        if (event.getSource() instanceof ChatHistory && ((ChatHistory) event.getSource()).getMessage() != null) {
            mongoOperations.save(((ChatHistory) event.getSource()).getMessage());
        }
    }
}
