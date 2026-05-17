package com.platform.voice.service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.platform.voice.config.StorageProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.TranscriptionRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class TranscriptionStorageService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionStorageService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;
    private final Counter storageCounter;
    private final Counter storageErrorCounter;

    public TranscriptionStorageService(StorageProperties storageProperties, MeterRegistry meterRegistry) {
        this.storageProperties = storageProperties;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.storageCounter = Counter.builder("voice.storage.records.saved")
                .description("Number of transcription records saved")
                .register(meterRegistry);
        this.storageErrorCounter = Counter.builder("voice.storage.errors")
                .description("Number of storage errors")
                .register(meterRegistry);
    }

    public TranscriptionRecord saveTranscription(
            String sessionId,
            byte[] audioData,
            String transcript,
            UserContext context,
            double confidence,
            long audioDurationMs,
            String provider
    ) {
        String recordId = UUID.randomUUID().toString();
        String datePath = LocalDate.now().format(DATE_FORMAT);

        String userIdHash = hashIdentifier(context.userId());
        String tenantIdHash = hashIdentifier(context.tenantId() != null ? context.tenantId() : "default");

        String audioFilePath = null;
        String checksum = null;

        if (audioData != null && audioData.length > 0) {
            audioFilePath = saveAudioFile(audioData, userIdHash, datePath, recordId);
            checksum = calculateChecksum(audioData);
        }

        TranscriptionRecord record = new TranscriptionRecord(
            recordId,
            sessionId,
            userIdHash,
            tenantIdHash,
            context.targetLanguage(),
            context.targetLevel(),
            provider,
            transcript,
            confidence,
            audioDurationMs,
            context.planType(),
            Instant.now(),
            audioFilePath,
            checksum,
            new TranscriptionRecord.Metadata(
                "wav",
                audioData != null ? audioData.length : 0,
                "voice_input",
                "WebSocket",
                String.valueOf(audioDurationMs)
            )
        );

        saveRecordMetadata(record, userIdHash, datePath, recordId);

        storageCounter.increment();
        log.info("Saved transcription record: {} for user: {}", recordId, userIdHash);

        return record;
    }

    private String saveAudioFile(byte[] audioData, String userIdHash, String datePath, String recordId) {
        try {
            Path baseDir = Paths.get(storageProperties.basePath());
            Path userDir = baseDir.resolve(userIdHash).resolve(datePath);
            Files.createDirectories(userDir);

            Path audioFile = userDir.resolve(recordId + ".wav");
            Files.write(audioFile, audioData);

            log.debug("Saved audio file: {}", audioFile);
            return audioFile.toString();

        } catch (IOException e) {
            log.error("Failed to save audio file: {}", e.getMessage());
            storageErrorCounter.increment();
            return null;
        }
    }

    private void saveRecordMetadata(TranscriptionRecord record, String userIdHash, String datePath, String recordId) {
        try {
            Path baseDir = Paths.get(storageProperties.basePath());
            Path metaDir = baseDir.resolve(userIdHash).resolve(datePath).resolve("metadata");
            Files.createDirectories(metaDir);

            Path metaFile = metaDir.resolve(recordId + ".json");
            String json = objectMapper.writeValueAsString(record);
            Files.writeString(metaFile, json, StandardCharsets.UTF_8);

            log.debug("Saved metadata file: {}", metaFile);

        } catch (IOException e) {
            log.error("Failed to save metadata: {}", e.getMessage());
            storageErrorCounter.increment();
        }
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
            log.warn("SHA-256 not available, using truncated identifier");
            return identifier.substring(0, Math.min(16, identifier.length()));
        }
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "checksum-unavailable";
        }
    }

    public boolean shouldRecord(String planType) {
        return storageProperties.transcription().enabled() && 
               storageProperties.retention().free() > 0 ||
               "STANDARD".equalsIgnoreCase(planType) ||
               "PREMIUM".equalsIgnoreCase(planType);
    }

    public int getRetentionDays(String planType) {
        return switch (planType.toUpperCase()) {
            case "FREE" -> storageProperties.retention().free();
            case "STANDARD" -> storageProperties.retention().standard();
            case "PREMIUM", "ENTERPRISE" -> storageProperties.retention().premium();
            default -> storageProperties.retention().standard();
        };
    }
}