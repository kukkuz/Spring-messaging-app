package com.kukkuz.chat.web;

import com.kukkuz.chat.domain.ChatTypingStatus;
import com.kukkuz.chat.event.LoginEvent;
import com.kukkuz.chat.event.ParticipantRepository;
import com.kukkuz.chat.mongo.model.ChatMessage;
import com.kukkuz.chat.mongo.repository.ChatHistoryRepository;
import com.kukkuz.chat.mongo.model.ChatHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;

/**
 * Controller that handles WebSocket chat messages
 */
@Controller
public class ChatController {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private MongoOperations mongoOperations;

    @PostConstruct
    private void initMongoTable() {
        if (!mongoOperations.collectionExists(ChatHistory.class)) {
            mongoOperations.createCollection(ChatHistory.class);
        }
    }

    @SubscribeMapping("/chat.participants")
    public Collection<LoginEvent> retrieveParticipants() {
        return participantRepository.getActiveSessions().values();
    }

    @SubscribeMapping("/chat.history.{username}")
    @SendToUser(value = "/exchange/amq.direct/chat.history")
    public Collection<ChatHistory> retrieveHistory(@DestinationVariable("username") String username, Principal principal) {
        Query query = new Query()
                .addCriteria(Criteria.where("to").is(username))
                .addCriteria(Criteria.where("from").is(principal.getName()));
        return mongoOperations.find(query, ChatHistory.class);
    }

    @SubscribeMapping("/chat.typing")
    public void typing(@Payload ChatTypingStatus status) {
        simpMessagingTemplate.convertAndSend("/user/" + status.getTo() + "/exchange/amq.direct/chat.typing", status);
    }

    @MessageMapping("/chat.private.{username}")
    public void filterPrivateMessage(@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {

        message.setUsername(principal.getName());
        // Destination message handler converts it /user/exchange.amq.direct/chat.message for the particular session
        simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/chat.message", message);

        // save the received message
        ChatHistory history = new ChatHistory();
        history.setMessage(message);
        history.setTo(principal.getName());
        history.setFrom(username);
        history.setIndex(new Date().getTime());
        chatHistoryRepository.insert(history);

        // save the sent message
        history = new ChatHistory();
        history.setMessage(message);
        history.setTo(username);
        history.setFrom(principal.getName());
        history.setIndex(new Date().getTime());
        chatHistoryRepository.insert(history);
    }

    @MessageExceptionHandler
    // this prefixes /user/{username}, broadcast=false targets the specific session only
    @SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
    public String handleProfanity(Exception e) {
        return e.getMessage();
    }
}
