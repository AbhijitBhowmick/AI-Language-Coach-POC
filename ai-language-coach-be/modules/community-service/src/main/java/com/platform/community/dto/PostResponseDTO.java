package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostResponseDTO(
        UUID id,
        UUID communityId,
        UUID userId,
        String content,
        int likesCount,
        int commentsCount,
        LocalDateTime createdAt
) {}