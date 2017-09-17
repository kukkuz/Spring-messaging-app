package com.kukkuz.chat.mongo.model;

import com.kukkuz.chat.mongo.annotation.CascadeSave;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ChatHistory {

    @Id
    private String id;

    @Indexed(direction = IndexDirection.DESCENDING)
    private long index;

    /**
     * History with this user
     */
    private String to;

    private String from;

    @DBRef
    @CascadeSave
    private ChatMessage message;

    public ChatHistory() {
    }

    @PersistenceConstructor
    public ChatHistory(final long index, final String to, final ChatMessage message) {
        this.index = index;
        this.to = to;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
