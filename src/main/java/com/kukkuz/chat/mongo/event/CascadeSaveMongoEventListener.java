package com.kukkuz.chat.mongo.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.util.ReflectionUtils;

public class CascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        ReflectionUtils.doWithFields(event.getSource().getClass(), new CascadeCallback(event.getSource(), mongoOperations));
    }
}