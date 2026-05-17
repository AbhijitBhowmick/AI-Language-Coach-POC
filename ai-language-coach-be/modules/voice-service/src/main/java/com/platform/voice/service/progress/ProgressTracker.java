package com.platform.voice.service.progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.platform.voice.config.StorageProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;
import com.platform.voice.model.UserProgress;
import com.platform.voice.model.UserProgress.LevelProgress;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ProgressTracker {

    private static final Logger log = LoggerFactory.getLogger(ProgressTracker.class);
    private static final String PROGRESS_KEY_PREFIX = "voice:progress:";

    private final RedisTemplate<String, Object> valkeyTemplate;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;
    private final Counter progressUpdateCounter;

    public ProgressTracker(
            RedisTemplate<String, Object> valkeyTemplate,
            StorageProperties storageProperties,
            MeterRegistry meterRegistry) {
        this.valkeyTemplate = valkeyTemplate;
        this.storageProperties = storageProperties;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.progressUpdateCounter = Counter.builder("voice.progress.updates")
                .description("Number of progress updates")
                .register(meterRegistry);
    }

    public UserProgress getProgress(UserContext context) {
        String userHash = hashIdentifier(context.userId());
        String key = buildProgressKey(context.targetLanguage(), userHash);

        Object cached = valkeyTemplate.opsForValue().get(key);
        if (cached instanceof UserProgress progress) {
            return progress;
        }

        return UserProgress.create(userHash, context.targetLanguage(), context.targetLevel());
    }

    public UserProgress updateProgress(
            UserContext context,
            String transcript,
            AnalysisResult analysis
    ) {
        progressUpdateCounter.increment();

        String userHash = hashIdentifier(context.userId());
        UserProgress current = getProgress(context);

        long newTotalSessions = current.totalSessions() + 1;
        long newTotalTranscriptions = current.totalTranscriptions() + 1;
        long newTotalAudioDuration = current.totalAudioDurationMs() + 
            (analysis != null && analysis.progress() != null ? 
                analysis.progress().audioDurationMs() : 0);

        List<String> updatedMistakes = updateMistakes(current.commonMistakes(), analysis);
        Map<String, Long> updatedWordFreq = updateWordFrequency(current.wordFrequency(), transcript);

        LevelProgress levelProgress = calculateLevelProgress(
            current.levelProgress(),
            current.targetLevel(),
            analysis
        );

        UserProgress updated = new UserProgress(
            userHash,
            context.targetLanguage(),
            context.targetLevel(),
            newTotalSessions,
            newTotalTranscriptions,
            newTotalAudioDuration,
            updatedMistakes,
            updatedWordFreq,
            levelProgress,
            Instant.now(),
            current.createdAt()
        );

        saveProgress(context, updated);

        log.info("Updated progress for user: {}, sessions: {}", userHash, newTotalSessions);

        return updated;
    }

    private List<String> updateMistakes(List<String> existingMistakes, AnalysisResult analysis) {
        List<String> updated = new ArrayList<>(existingMistakes);

        if (analysis != null && analysis.mistakes() != null) {
            for (var mistake : analysis.mistakes()) {
                if (mistake.severity().equals("high") || mistake.severity().equals("medium")) {
                    String mistakeKey = mistake.type() + ":" + mistake.description();
                    if (!updated.contains(mistakeKey)) {
                        updated.add(mistakeKey);
                    }
                }
            }
        }

        if (updated.size() > 50) {
            return updated.subList(updated.size() - 50, updated.size());
        }

        return updated;
    }

    private Map<String, Long> updateWordFrequency(Map<String, Long> existing, String transcript) {
        Map<String, Long> updated = new HashMap<>(existing);

        if (transcript != null) {
            String[] words = transcript.toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 2) {
                    updated.merge(word, 1L, Long::sum);
                }
            }
        }

        if (updated.size() > 1000) {
            return sortAndTruncate(updated, 500);
        }

        return updated;
    }

    private LevelProgress calculateLevelProgress(
            LevelProgress current,
            String targetLevel,
            AnalysisResult analysis
    ) {
        int a1Score = current.a1Score();
        int a2Score = current.a2Score();
        int b1Score = current.b1Score();
        int b2Score = current.b2Score();
        long sessions = current.sessionsAtLevel() + 1;

        if (analysis != null && analysis.cefrAlignment() != null) {
            String suggestedLevel = analysis.cefrAlignment().suggestedLevel();
            switch (suggestedLevel.toUpperCase()) {
                case "A1" -> a1Score = Math.min(100, a1Score + 5);
                case "A2" -> a2Score = Math.min(100, a2Score + 5);
                case "B1" -> b1Score = Math.min(100, b1Score + 5);
                case "B2" -> b2Score = Math.min(100, b2Score + 5);
            }
        }

        return new LevelProgress(targetLevel, a1Score, a2Score, b1Score, b2Score, sessions);
    }

    private Map<String, Long> sortAndTruncate(Map<String, Long> map, int keepTopN) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(keepTopN)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    private void saveProgress(UserContext context, UserProgress progress) {
        String userHash = hashIdentifier(context.userId());
        String key = buildProgressKey(context.targetLanguage(), userHash);

        try {
            String json = objectMapper.writeValueAsString(progress);
            valkeyTemplate.opsForValue().set(key, json, 30, TimeUnit.DAYS);

            saveProgressToFile(userHash, context.targetLanguage(), json);

        } catch (Exception e) {
            log.error("Failed to save progress: {}", e.getMessage());
        }
    }

    private void saveProgressToFile(String userHash, String targetLanguage, String json) {
        try {
            Path baseDir = Paths.get(storageProperties.basePath());
            Path progressDir = baseDir.resolve(userHash).resolve(targetLanguage.toLowerCase());
            Files.createDirectories(progressDir);

            Path progressFile = progressDir.resolve("progress.json");
            Files.writeString(progressFile, json, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.warn("Failed to save progress to file: {}", e.getMessage());
        }
    }

    private String buildProgressKey(String targetLanguage, String userHash) {
        return PROGRESS_KEY_PREFIX + targetLanguage.toLowerCase() + ":" + userHash;
    }

    private String hashIdentifier(String identifier) {
        if (identifier == null) return "anonymous";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(identifier.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return identifier.substring(0, Math.min(16, identifier.length()));
        }
    }

    public Map<String, Object> getProgressSummary(UserContext context) {
        UserProgress progress = getProgress(context);

        return Map.of(
            "userHash", progress.userIdHash(),
            "targetLanguage", progress.targetLanguage(),
            "currentLevel", progress.levelProgress().currentLevel(),
            "totalSessions", progress.totalSessions(),
            "totalTranscriptions", progress.totalTranscriptions(),
            "totalAudioMinutes", progress.totalAudioDurationMs() / 60000,
            "levelProgress", Map.of(
                "A1", progress.levelProgress().a1Score(),
                "A2", progress.levelProgress().a2Score(),
                "B1", progress.levelProgress().b1Score(),
                "B2", progress.levelProgress().b2Score()
            ),
            "commonMistakes", progress.commonMistakes().size(),
            "topWords", progress.wordFrequency().entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(20)
                    .map(Map.Entry::getKey)
                    .toList()
        );
    }
}