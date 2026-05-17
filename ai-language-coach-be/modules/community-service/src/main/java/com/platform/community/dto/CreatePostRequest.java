package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatePostRequest(
        String content
) {
    public String getContent() { return content; }
}