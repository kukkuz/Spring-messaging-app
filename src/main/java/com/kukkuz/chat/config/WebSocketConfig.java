package com.kukkuz.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp").withSockJS()
                .setStreamBytesLimit(128 * 1024) // default 128K
                .setHttpMessageCacheSize(100); // default 100
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //registry.enableSimpleBroker("/queue/", "/topic/", "/exchange/");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(60 * 1000)
                // send message size 200MB : default is 512KB
                // If message sending is slow, or at least slower than rate of messages sending,
                // subsequent messages are buffered until either the {@code sendTimeLimit}
                // or the {@code sendBufferSizeLimit} are reached at which point the session
                // state is cleared and an attempt is made to close the session.
                .setSendBufferSizeLimit(200 * 1024 * 1024)
                // max message size 200MB : default is 64KB
                // send from SockJS in 16K frames
                .setMessageSizeLimit(200 * 1024 * 1024);
    }


}