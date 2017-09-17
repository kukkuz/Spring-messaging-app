package com.kukkuz.chat.mongo.converter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.kukkuz.chat.mongo.model.ChatHistory;
import com.kukkuz.chat.mongo.model.ChatMessage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChatHistoryWriterConverter implements Converter<ChatHistory, DBObject> {

    @Override
    public DBObject convert(final ChatHistory chatHistory) {
        final DBObject dbObject = new BasicDBObject();
        dbObject.put("index", chatHistory.getIndex());
        dbObject.put("to", chatHistory.getTo());
        dbObject.put("from", chatHistory.getFrom());
        ChatMessage message = chatHistory.getMessage();
        if (message != null) {
            final DBObject messageDbObject = new BasicDBObject();
            messageDbObject.put("username", message.getUsername());
            messageDbObject.put("to", message.getTo());
            messageDbObject.put("message", message.getMessage());
            messageDbObject.put("date", message.getDate());
            messageDbObject.put("video", message.isVideo());
            messageDbObject.put("image", message.isImage());
            messageDbObject.put("audio", message.isAudio());
            dbObject.put("message", messageDbObject);
        }
        dbObject.removeField("_class");
        return dbObject;
    }

}
