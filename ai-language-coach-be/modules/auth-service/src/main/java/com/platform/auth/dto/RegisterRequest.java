package com.platform.auth.dto;

public record RegisterRequest(
    String email,
    String password,
    String firstName,
    String lastName,
    String role,
    String businessName,
    String targetLanguage,
    String targetLevel,
    String nativeLanguage
) {}