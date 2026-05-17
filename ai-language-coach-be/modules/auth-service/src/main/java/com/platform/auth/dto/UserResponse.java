package com.platform.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String provider,
        String tenantId,
        Set<Role> roles,
        User.UserStatus status,
        String businessName,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime approvedAt
) {}