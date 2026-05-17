package com.platform.diagnostic.service;

import com.platform.common.model.LearningContext;
import com.platform.diagnostic.dto.DiagnosticQuestion;
import com.platform.diagnostic.entity.QuestionEntity;
import com.platform.diagnostic.repository.QuestionEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankService.class);

    private final QuestionEntityRepository questionRepository;

    public QuestionBankService(QuestionEntityRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

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

        AtomicInteger counter = new AtomicInteger(1);
        return entities.stream()
                .map(entity -> mapToDiagnosticQuestion(entity, counter.getAndIncrement()))
                .collect(Collectors.toList());
    }

    public List<DiagnosticQuestion> getQuestions(String targetLanguage, String level) {
        return getQuestions(new LearningContext(null, targetLanguage, level));
    }

    public List<String> getAvailableLanguages() {
        return questionRepository.findDistinctLanguages();
    }

    public List<String> getAvailableLevels(String language) {
        return questionRepository.findLevelsForLanguage(language);
    }

    private DiagnosticQuestion mapToDiagnosticQuestion(QuestionEntity entity, int questionNumber) {
        return DiagnosticQuestion.builder()
                .questionNumber(questionNumber)
                .targetLanguage(entity.getTargetLanguage())
                .targetLevel(entity.getTargetLevel())
                .nativeLanguage(entity.getNativeLanguage())
                .questionText(entity.getQuestionText())
                .situation(entity.getSituation())
                .options(parseOptions(entity.getOptions()))
                .correctAnswer(entity.getCorrectAnswer())
                .explanation(entity.getExplanation())
                .build();
    }

    private List<String> parseOptions(String options) {
        if (options == null || options.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(options.split("\\|"));
    }
}