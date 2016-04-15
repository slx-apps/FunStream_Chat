package com.slx.funstream.chat.events;


import com.slx.funstream.rest.model.Smile;

import java.util.Map;

public class SmileLoadEvent {
    private Map<String, Smile> map;

    public SmileLoadEvent() {
    }

    public SmileLoadEvent(Map<String, Smile> map) {
        this.map = map;
    }

    public Map<String, Smile> getSmiles() {
        return map;
    }

    public void setSmiles(Map<String, Smile> map) {
        this.map = map;
    }
}
