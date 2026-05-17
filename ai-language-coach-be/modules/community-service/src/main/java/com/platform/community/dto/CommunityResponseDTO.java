package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunityResponseDTO(
        UUID id,
        String name,
        String description,
        String tenantId,
        UUID createdBy,
        UUID adminTeacherId,
        boolean isActive,
        LocalDateTime createdAt,
        Boolean isJoined
) {}