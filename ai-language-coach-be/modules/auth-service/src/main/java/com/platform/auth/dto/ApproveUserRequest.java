package com.platform.auth.dto;

import java.util.UUID;

public record ApproveUserRequest(UUID userId, String comment) {}