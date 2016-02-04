package com.slx.funstream.rest.model;


import com.slx.funstream.model.Stream;

import java.util.List;

public class ContentResponse {

    List<Stream> content;

    public ContentResponse() {}

    public List<Stream> getContent() {
        return content;
    }

    public void setContent(List<Stream> content) {
        this.content = content;
    }
}
