package com.kukkuz.chat.mongo.repository;

import com.kukkuz.chat.mongo.model.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
}
