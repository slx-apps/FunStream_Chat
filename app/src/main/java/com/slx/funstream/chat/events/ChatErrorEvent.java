package com.slx.funstream.chat.events;


public class ChatErrorEvent {
    private String message;

    public ChatErrorEvent() {
    }

    public ChatErrorEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
