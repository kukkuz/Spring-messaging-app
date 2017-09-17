package com.kukkuz.chat.config;

import com.kukkuz.chat.event.PresenceEventListener;
import com.kukkuz.chat.event.ParticipantRepository;
import org.springframework.context.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class ChatConfig {

    public static class Destinations {

        private static final String LOGIN = "/topic/chat.login";
        private static final String LOGOUT = "/topic/chat.logout";
    }

    @Bean
    @Description("Tracks user presence (join / leave) and broacasts it to all connected users")
    public PresenceEventListener presenceEventListener(SimpMessagingTemplate messagingTemplate) {
        PresenceEventListener presence = new PresenceEventListener(messagingTemplate, participantRepository());
        presence.setLoginDestination(Destinations.LOGIN);
        presence.setLogoutDestination(Destinations.LOGOUT);
        return presence;
    }

    @Bean
    @Description("Keeps connected users")
    public ParticipantRepository participantRepository() {
        return new ParticipantRepository();
    }
}
