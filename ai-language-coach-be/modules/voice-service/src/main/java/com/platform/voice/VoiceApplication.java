package com.platform.voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.platform.voice.config.VoiceProperties;
import com.platform.voice.config.StorageProperties;
import com.platform.voice.config.AnalyticsProperties;

@SpringBootApplication(scanBasePackages = "com.platform")
@EnableConfigurationProperties({
    VoiceProperties.class,
    StorageProperties.class,
    AnalyticsProperties.class
})
@EnableScheduling
public class VoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceApplication.class, args);
    }
}