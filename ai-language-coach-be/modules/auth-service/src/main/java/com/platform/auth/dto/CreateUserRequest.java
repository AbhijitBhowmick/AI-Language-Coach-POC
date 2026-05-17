package com.platform.auth.dto;

public record CreateUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String role,
        String businessName,
        String targetLanguage,
        String targetLevel,
        String nativeLanguage
) {
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getBusinessName() { return businessName; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
}