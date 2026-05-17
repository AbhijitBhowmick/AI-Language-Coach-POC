package com.platform.auth.dto;

import java.util.UUID;

public record RejectUserRequest(UUID userId, String reason) {}