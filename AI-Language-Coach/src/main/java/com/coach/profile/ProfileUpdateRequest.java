package com.coach.profile;

public class ProfileUpdateRequest {
    private String targetLanguage;
    private String targetLevel;
    private String nativeLanguage;
    private PlanType planType;

    public ProfileUpdateRequest() {}

    public ProfileUpdateRequest(String targetLanguage, String targetLevel, String nativeLanguage, PlanType planType) {
        this.targetLanguage = targetLanguage;
        this.targetLevel = targetLevel;
        this.nativeLanguage = nativeLanguage;
        this.planType = planType;
    }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
}