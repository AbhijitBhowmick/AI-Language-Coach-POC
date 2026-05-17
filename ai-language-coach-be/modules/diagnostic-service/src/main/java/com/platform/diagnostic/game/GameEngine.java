package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.GameQuestion;
import com.platform.diagnostic.dto.GameState;
import com.platform.diagnostic.dto.GameAnswer;
import com.platform.diagnostic.dto.GameResult;
import com.platform.diagnostic.dto.GameRenderData;
import com.platform.diagnostic.dto.ValidationResult;

public interface GameEngine {

    String getTemplateId();

    GameType getGameType();

    GameRenderData render(GameQuestion question, GameState state);

    GameResult evaluate(GameAnswer answer, GameQuestion question);

    ValidationResult validateAnswer(GameAnswer answer);

    int getDefaultTimeSeconds();

    boolean supportsLives();

    boolean supportsBranching();
}