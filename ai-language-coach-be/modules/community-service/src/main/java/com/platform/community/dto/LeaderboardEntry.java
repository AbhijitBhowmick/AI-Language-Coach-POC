package com.platform.community.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeaderboardEntry(
        UUID userId,
        long totalScore
) {}