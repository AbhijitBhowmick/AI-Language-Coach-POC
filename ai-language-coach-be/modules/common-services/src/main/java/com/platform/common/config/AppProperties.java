package com.platform.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.defaults")
public class AppProperties {

    private String targetLanguage = "Czech";
    private String targetLevel = "A1";
    private String nativeLanguage = "en";

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }

    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
}
