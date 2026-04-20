package com.coach.common.ai;

import com.coach.profile.UserProfile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PromptTemplateService {

    private static final String TEMPLATE_PATH = "classpath:prompts/linguistic_bridge_template.txt";
    private static final int MAX_TEMPLATE_SIZE = 4096;

    public String buildSystemPrompt(UserProfile profile) {
        String template = loadTemplate();
        
        String targetLang = profile.getTargetLanguage();
        String targetLevel = profile.getTargetLevel();
        String nativeLang = profile.getNativeLanguage();
        
        return template
                .replace("{nativeLanguage}", nativeLang != null ? nativeLang : "en")
                .replace("{targetLanguage}", targetLang != null ? targetLang : "Czech")
                .replace("{currentLevel}", targetLevel != null ? targetLevel : "A1")
                .replace("{proficiencyScore}", String.valueOf(profile.getReadinessScore()));
    }

    private String loadTemplate() {
        try {
            Path path = Paths.get("src/main/resources/prompts/linguistic_bridge_template.txt");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                return content.length() > MAX_TEMPLATE_SIZE ? content.substring(0, MAX_TEMPLATE_SIZE) : content;
            }
        } catch (IOException e) {
            return getDefaultTemplate();
        }
        return getDefaultTemplate();
    }

    private String getDefaultTemplate() {
        return """
            You are a {targetLanguage} language tutor for {currentLevel} level students whose native language is {nativeLanguage}.
            Be patient, encouraging, and use simple language.
            When correcting mistakes, provide the correct form naturally.
            Always explain grammar in a friendly, understandable way.
            Use linguistic bridges to explain concepts familiar to {nativeLanguage} speakers when helpful.
            """;
    }
}