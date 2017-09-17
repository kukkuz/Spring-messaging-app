package com.kukkuz.chat.domain;

/**
 * Created by kukku on 14/6/16.
 */
public class ChatTypingStatus {

    private String from;

    private String to;

    private boolean typing;

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
