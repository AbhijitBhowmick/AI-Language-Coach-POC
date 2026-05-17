package com.platform.voice.service.tts;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PiperService implements TextToSpeechService {

    private static final Logger log = LoggerFactory.getLogger(PiperService.class);

    private final VoiceProperties voiceProperties;
    private final Timer piperTimer;
    private final Path piperBinaryPath;
    private final Path modelsDirectory;

    public PiperService(VoiceProperties voiceProperties, MeterRegistry meterRegistry) {
        this.voiceProperties = voiceProperties;
        this.piperTimer = Timer.builder("voice.tts.piper.duration")
                .description("Time to synthesize speech with Piper")
                .register(meterRegistry);
        this.piperBinaryPath = Path.of(System.getProperty("user.home"), ".local", "bin", "piper");
        this.modelsDirectory = Path.of(System.getProperty("user.home"), ".local", "share", "piper", "models");
    }

    @Override
    public byte[] synthesize(String text, UserContext context) {
        log.info("Synthesizing TTS with Piper for user {} in {}", context.userId(), context.targetLanguage());
        return piperTimer.record(() -> doSynthesize(text, context));
    }

    private byte[] doSynthesize(String text, UserContext context) {
        String modelName = resolveModel(context);
        Path modelPath = modelsDirectory.resolve(modelName + ".onnx");
        
        if (!Files.exists(modelPath)) {
            log.warn("Piper model not found: {}, falling back to placeholder", modelPath);
            return generatePlaceholderAudio(text);
        }

        if (!Files.exists(piperBinaryPath)) {
            log.warn("Piper binary not found: {}, falling back to placeholder", piperBinaryPath);
            return generatePlaceholderAudio(text);
        }

        try {
            Path tempInput = Files.createTempFile("piper_input_", ".txt");
            Path tempOutput = Files.createTempFile("piper_output_", ".wav");
            
            try {
                Files.writeString(tempInput, text);
                
                ProcessBuilder pb = new ProcessBuilder(
                    piperBinaryPath.toString(),
                    "-m", modelPath.toString(),
                    "-f", tempOutput.toString()
                );
                pb.redirectErrorStream(true);
                
                Process process = pb.start();
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("Piper: {}", line);
                    }
                }
                
                int exitCode = process.waitFor();
                
                if (exitCode == 0 && Files.exists(tempOutput)) {
                    log.info("Piper synthesis completed for user {}", context.userId());
                    return Files.readAllBytes(tempOutput);
                }
                
                log.warn("Piper synthesis failed with exit code {}, using placeholder", exitCode);
                return generatePlaceholderAudio(text);
                
            } finally {
                Files.deleteIfExists(tempInput);
                Files.deleteIfExists(tempOutput);
            }
            
        } catch (Exception e) {
            log.error("Piper synthesis failed: {}", e.getMessage(), e);
            return generatePlaceholderAudio(text);
        }
    }

    private String resolveModel(UserContext context) {
        String targetLanguage = context.targetLanguage();
        String targetLevel = context.targetLevel();
        String nativeLanguage = context.nativeLanguage();
        
        if ("czech".equalsIgnoreCase(targetLanguage)) {
            if ("A1".equalsIgnoreCase(targetLevel) || "A2".equalsIgnoreCase(targetLevel)) {
                return "cs_CZ-cz_CZ-medium.onnx";
            }
            return "cs_CZ-cz_CZ-medium.onnx";
        }
        
        if ("en".equalsIgnoreCase(nativeLanguage)) {
            return "en_US-joe-medium.onnx";
        }
        
        if ("hi".equalsIgnoreCase(nativeLanguage) || "bn".equalsIgnoreCase(nativeLanguage)) {
            return "en_US-lessac-medium.onnx";
        }
        
        return "en_US-joe-medium.onnx";
    }

    private byte[] generatePlaceholderAudio(String text) {
        int audioDataSeconds = Math.max(1, text.length() / 10);
        int audioDataSize = audioDataSeconds * 22050 * 2;
        return createMinimalWav(audioDataSize);
    }

    private byte[] createMinimalWav(int dataSize) {
        int totalSize = 36 + dataSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
            dos.writeBytes("RIFF");
            dos.writeInt(Integer.reverseBytes(totalSize));
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(Integer.reverseBytes(16));
            dos.writeShort(Short.reverseBytes((short) 1));
            dos.writeShort(Short.reverseBytes((short) 1));
            dos.writeInt(Integer.reverseBytes(22050));
            dos.writeInt(Integer.reverseBytes(22050 * 2));
            dos.writeShort(Short.reverseBytes((short) 2));
            dos.writeShort(Short.reverseBytes((short) 8));
            dos.writeBytes("data");
            dos.writeInt(Integer.reverseBytes(dataSize));
        } catch (IOException e) {
            log.warn("Failed to create WAV header: {}", e.getMessage());
        }
        
        return baos.toByteArray();
    }

    private char intToChar(int value) {
        return (char) (value & 0xff);
    }

    @Override
    public String getProviderName() {
        return "piper";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return "FREE".equalsIgnoreCase(planType);
    }
}