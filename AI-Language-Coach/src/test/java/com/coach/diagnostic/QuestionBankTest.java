package com.coach.diagnostic;

import com.coach.common.LearningContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionBankTest {

    @Mock
    private QuestionEntityRepository questionRepository;

    private QuestionBank questionBank;

    @BeforeEach
    void setUp() {
        questionBank = new QuestionBank(questionRepository);
    }

    @Test
    void shouldLoadQuestionsFromDatabase() {
        LearningContext context = new LearningContext("Czech", "A1", "en");

        when(questionRepository.findActiveQuestions("Czech", "A1"))
                .thenReturn(List.of());

        List<DiagnosticQuestion> questions = questionBank.getQuestions(context);

        verify(questionRepository).findActiveQuestions("Czech", "A1");
    }

    @Test
    void shouldReturnAvailableLanguages() {
        when(questionRepository.findDistinctLanguages())
                .thenReturn(List.of("Czech", "German", "Dutch"));

        List<String> languages = questionBank.getAvailableLanguages();

        assertTrue(languages.contains("Czech"));
        assertTrue(languages.contains("German"));
    }

    @Test
    void shouldReturnAvailableLevels() {
        when(questionRepository.findLevelsForLanguage("Czech"))
                .thenReturn(List.of("A1", "A2"));

        List<String> levels = questionBank.getAvailableLevels("Czech");

        assertTrue(levels.contains("A1"));
        assertTrue(levels.contains("A2"));
    }

    @Test
    void shouldGetQuestionsByTargetLanguageAndLevel() {
        List<DiagnosticQuestion> questions = questionBank.getQuestions("German", "A1");

        verify(questionRepository).findActiveQuestions("German", "A1");
    }
}