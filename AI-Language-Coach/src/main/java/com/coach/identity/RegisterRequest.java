package com.coach.identity;

public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String nativeLanguage;
    private String role; // STUDENT or ADMIN

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public String getRole() { return role != null ? role : "STUDENT"; }
    public void setRole(String role) { this.role = role; }
}