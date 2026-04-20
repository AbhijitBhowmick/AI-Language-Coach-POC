package com.coach.diagnostic;

import com.coach.common.LearningContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionBank {

    private static final Logger log = LoggerFactory.getLogger(QuestionBank.class);

    private final QuestionEntityRepository questionRepository;

    public QuestionBank(QuestionEntityRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Cacheable(value = "questions", key = "#context.targetLanguage + '_' + #context.targetLevel")
    public List<DiagnosticQuestion> getQuestions(LearningContext context) {
        log.info("Loading questions from DB for: {}/{}", context.targetLanguage(), context.targetLevel());
        
        List<QuestionEntity> entities = questionRepository.findActiveQuestions(
            context.targetLanguage(), 
            context.targetLevel()
        );

        if (entities.isEmpty()) {
            log.warn("No questions found for {}/{}, loading defaults", context.targetLanguage(), context.targetLevel());
            entities = questionRepository.findActiveQuestions("Czech", "A1");
        }

        return entities.stream()
                .map(this::mapToDiagnosticQuestion)
                .collect(Collectors.toList());
    }

    public List<DiagnosticQuestion> getQuestions(String targetLanguage, String level) {
        return getQuestions(new LearningContext(targetLanguage, level, "en"));
    }

    public List<String> getAvailableLanguages() {
        return questionRepository.findDistinctLanguages();
    }

    public List<String> getAvailableLevels(String language) {
        return questionRepository.findLevelsForLanguage(language);
    }

    private DiagnosticQuestion mapToDiagnosticQuestion(QuestionEntity entity) {
        return DiagnosticQuestion.builder()
                .questionNumber(entity.getId().hashCode())
                .type(parseQuestionType(entity.getQuestionType()))
                .targetLanguage(entity.getTargetLanguage())
                .targetLevel(entity.getTargetLevel())
                .nativeLanguage(entity.getNativeLanguage())
                .questionText(entity.getQuestionText())
                .situation(entity.getSituation())
                .options(parseOptions(entity.getOptions()))
                .correctAnswer(entity.getCorrectAnswer())
                .explanation(entity.getExplanation())
                .level(entity.getTargetLevel())
                .linguisticBridge("en", entity.getLinguisticBridgeEn())
                .linguisticBridge("bn", entity.getLinguisticBridgeBn())
                .linguisticBridge("hi", entity.getLinguisticBridgeHi())
                .linguisticBridge("te", entity.getLinguisticBridgeTe())
                .linguisticBridge("uk", entity.getLinguisticBridgeUk())
                .build();
    }

    private DiagnosticQuestion.QuestionType parseQuestionType(String type) {
        if (type == null) return DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION;
        try {
            return DiagnosticQuestion.QuestionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION;
        }
    }

    private List<String> parseOptions(String options) {
        if (options == null || options.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(options.split("\\|"));
    }
}