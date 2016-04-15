package com.slx.funstream.chat.events;


public class ChatUserListEvent {
    private Integer[] users;

    public ChatUserListEvent() {}

    public ChatUserListEvent(Integer[] users) {
        this.users = users;
    }

    public Integer[] getUsers() {
        return users;
    }

    public void setUsers(Integer[] users) {
        this.users = users;
    }
}
