package com.platform.voice.service.tts;

import com.platform.voice.dto.UserContext;

public interface TextToSpeechService {

    byte[] synthesize(String text, UserContext context);

    String getProviderName();

    boolean supportsPlan(String planType);
}