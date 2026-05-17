package com.platform.voice.service.stt;

import com.platform.voice.dto.TranscriptionResult;
import com.platform.voice.dto.UserContext;

public interface SpeechToTextService {

    TranscriptionResult transcribe(byte[] audioData, UserContext context);

    String getProviderName();

    boolean supportsPlan(String planType);
}