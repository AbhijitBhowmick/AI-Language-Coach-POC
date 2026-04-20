package com.coach.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_CACHE_KEY = "system:config:";
    private static final Duration CONFIG_CACHE_TTL = Duration.ofHours(1);

    private final SystemConfigRepository configRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LanguageConfigRepository languageRepository;
    private final NativeLanguageRepository nativeLanguageRepository;

    private volatile String defaultTargetLanguage;
    private volatile String defaultTargetLevel;
    private volatile String defaultNativeLanguage;
    private volatile String linguisticBridgePrompt;
    private String cacheKey;

    public ConfigService(SystemConfigRepository configRepository, 
                       RedisTemplate<String, Object> redisTemplate,
                       LanguageConfigRepository languageRepository,
                       NativeLanguageRepository nativeLanguageRepository) {
        this.configRepository = configRepository;
        this.redisTemplate = redisTemplate;
        this.languageRepository = languageRepository;
        this.nativeLanguageRepository = nativeLanguageRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaults() {
        if (redisTemplate == null || redisTemplate.opsForValue() == null) {
            log.warn("Redis not available, using hardcoded defaults");
            this.defaultTargetLanguage = "Czech";
            this.defaultTargetLevel = "A1";
            this.defaultNativeLanguage = "en";
            this.linguisticBridgePrompt = "Explain {targetLanguage} grammar using concepts familiar to {nativeLanguage} speakers.";
            return;
        }

        log.info("Initializing system configuration from database...");
        loadDefaultsFromDatabase();
        log.info("Default configuration loaded: language={}, level={}, native={}",
                defaultTargetLanguage, defaultTargetLevel, defaultNativeLanguage);
    }

    public void loadDefaultsFromDatabase() {
        this.defaultTargetLanguage = getConfigValue("default.target.language", "Czech");
        this.defaultTargetLevel = getConfigValue("default.target.level", "A1");
        this.defaultNativeLanguage = getConfigValue("default.native.language", "en");
        this.linguisticBridgePrompt = getConfigValue("linguistic.bridge.prompt", 
            "Explain {targetLanguage} grammar using concepts familiar to {nativeLanguage} speakers. Use simple language.");
    }

    private String getConfigValue(String key, String fallback) {
        if (redisTemplate == null || redisTemplate.opsForValue() == null) {
            log.warn("Redis not available, using fallback for '{}': {}", key, fallback);
            return fallback;
        }

        String cacheKey = CONFIG_CACHE_KEY + key;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached.toString();
            }
        } catch (NullPointerException e) {
            log.warn("Redis opsForValue() returned null, using fallback for '{}': {}", key, fallback);
            return fallback;
        }

        return configRepository.findByConfigKeyAndActiveTrue(key)
                .map(config -> {
                    try {
                        redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CONFIG_CACHE_TTL);
                    } catch (NullPointerException e) {
                        log.warn("Failed to cache config '{}': {}", key, e.getMessage());
                    }
                    return config.getConfigValue();
                })
                .orElseGet(() -> {
                    log.warn("Config key '{}' not found, using fallback: {}", key, fallback);
                    return fallback;
                });
    }

    public String getDefaultTargetLanguage() {
        if (defaultTargetLanguage == null) loadDefaultsFromDatabase();
        return defaultTargetLanguage;
    }

    public String getDefaultTargetLevel() {
        if (defaultTargetLevel == null) loadDefaultsFromDatabase();
        return defaultTargetLevel;
    }

    public String getDefaultNativeLanguage() {
        if (defaultNativeLanguage == null) loadDefaultsFromDatabase();
        return defaultNativeLanguage;
    }

    public String getLinguisticBridgePrompt() {
        if (linguisticBridgePrompt == null) loadDefaultsFromDatabase();
        return linguisticBridgePrompt;
    }

    public String buildLinguisticBridgePrompt(String targetLanguage, String nativeLanguage) {
        return getLinguisticBridgePrompt()
                .replace("{targetLanguage}", targetLanguage)
                .replace("{nativeLanguage}", nativeLanguage);
    }

    public void reloadConfig() {
        redisTemplate.delete(CONFIG_CACHE_KEY + "*");
        loadDefaultsFromDatabase();
        log.info("Configuration reloaded from database");
    }

    public List<String> getAvailableLanguages() {
        return languageRepository.findByEnabledTrueOrderByDisplayOrder()
                .stream()
                .map(LanguageConfig::getLanguageCode)
                .distinct()
                .toList();
    }

    public List<String> getAvailableLevels(String languageCode) {
        return languageRepository.findByLanguageCodeAndEnabledTrue(languageCode)
                .stream()
                .map(LanguageConfig::getLevel)
                .toList();
    }

    public List<String> getAvailableNativeLanguages() {
        return nativeLanguageRepository.findByEnabledTrue()
                .stream()
                .map(NativeLanguage::getLanguageCode)
                .toList();
    }
}