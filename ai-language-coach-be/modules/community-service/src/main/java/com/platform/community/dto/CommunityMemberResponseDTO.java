package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunityMemberResponseDTO(
        UUID id,
        UUID communityId,
        UUID userId,
        String role,
        boolean blocked,
        LocalDateTime joinedAt
) {}