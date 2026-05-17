package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentResponseDTO(
        UUID id,
        UUID postId,
        UUID userId,
        String content,
        LocalDateTime createdAt
) {}