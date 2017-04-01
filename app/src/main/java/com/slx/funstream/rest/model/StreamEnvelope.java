package com.slx.funstream.rest.model;

import java.util.List;

public class StreamEnvelope<T> {
    private T response;

    public StreamEnvelope() {}

    public T response() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
