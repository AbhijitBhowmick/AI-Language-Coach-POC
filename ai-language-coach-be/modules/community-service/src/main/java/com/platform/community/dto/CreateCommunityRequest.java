package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCommunityRequest(
        String name,
        String description
) {
    public String getName() { return name; }
    public String getDescription() { return description; }
}