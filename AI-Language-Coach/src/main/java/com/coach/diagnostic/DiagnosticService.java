package com.coach.diagnostic;

import com.coach.common.LearningContext;
import com.coach.profile.ProfileService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiagnosticService {

    private static final String TEST_PREFIX = "diagnostic:test:";
    private static final Duration TEST_TTL = Duration.ofHours(2);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DiagnosticService.class);

    private final RedisTemplate<String, Object> valkeyTemplate;
    private final QuestionBank questionBank;
    private final ProfileService profileService;

    public DiagnosticService(RedisTemplate<String, Object> valkeyTemplate, QuestionBank questionBank, ProfileService profileService) {
        this.valkeyTemplate = valkeyTemplate;
        this.questionBank = questionBank;
        this.profileService = profileService;
    }

    public DiagnosticTest startTest(UUID userId, LearningContext context) {
        String key = TEST_PREFIX + userId.toString();
        
        List<DiagnosticQuestion> questions = questionBank.getQuestions(context);
        
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage(context.targetLanguage())
                .targetLevel(context.targetLevel())
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();

        valkeyTemplate.opsForValue().set(key, test, TEST_TTL);
        log.info("Started diagnostic test for user: {} with context: {}", userId, context);
        
        return test;
    }

    public DiagnosticTest getTest(UUID userId) {
        String key = TEST_PREFIX + userId.toString();
        Object result = valkeyTemplate.opsForValue().get(key);
        
        if (result instanceof DiagnosticTest) {
            return (DiagnosticTest) result;
        }
        
        return null;
    }

    public DiagnosticQuestion getCurrentQuestion(UUID userId) {
        DiagnosticTest test = getTest(userId);
        
        if (test == null || test.isCompleted()) {
            return null;
        }

        int index = test.getCurrentQuestionIndex();
        if (index >= 0 && index < test.getQuestions().size()) {
            return test.getQuestions().get(index);
        }
        
        return null;
    }

    public DiagnosticTest submitAnswer(UUID userId, AnswerSubmission submission) {
        String key = TEST_PREFIX + userId.toString();
        DiagnosticTest test = getTest(userId);
        
        if (test == null || test.isCompleted()) {
            throw new RuntimeException("No active test found for user: " + userId);
        }

        DiagnosticQuestion question = test.getQuestions().get(submission.getQuestionNumber() - 1);
        
        if (submission.getAnswer().equalsIgnoreCase(question.getCorrectAnswer()) ||
            submission.getAnswer().equalsIgnoreCase(question.getCorrectAnswer().split("/")[0])) {
            test.setCorrectAnswers(test.getCorrectAnswers() + 1);
        }

        test.setCurrentQuestionIndex(test.getCurrentQuestionIndex() + 1);

        if (test.getCurrentQuestionIndex() >= test.getQuestions().size()) {
            test.setCompleted(true);
            test.setCompletedAt(System.currentTimeMillis());
            
            double score = calculateScore(test);
            String recommendedLevel = determineLevel(score);
            
            LearningContext context = new LearningContext(
                test.getTargetLanguage(),
                recommendedLevel,
                test.getNativeLanguage()
            );
            
            profileService.updateReadinessScore(userId, score, context);
            
            log.info("Completed diagnostic test for user: {}. Score: {}, Level: {}", 
                    userId, score, recommendedLevel);
        }

        valkeyTemplate.opsForValue().set(key, test, TEST_TTL);
        return test;
    }

    public TestResult getTestResult(UUID userId) {
        DiagnosticTest test = getTest(userId);
        
        if (test == null) {
            throw new RuntimeException("No test found for user: " + userId);
        }

        double scorePercentage = calculateScore(test);
        String recommendedLevel = determineLevel(scorePercentage);

        List<TestResult.QuestionFeedback> feedback = test.getQuestions().stream()
                .map(q -> TestResult.QuestionFeedback.builder()
                        .questionNumber(q.getQuestionNumber())
                        .correctAnswer(q.getCorrectAnswer())
                        .explanation(q.getExplanation())
                        .build())
                .collect(Collectors.toList());

        return TestResult.builder()
                .targetLanguage(test.getTargetLanguage())
                .targetLevel(recommendedLevel)
                .totalQuestions(test.getQuestions().size())
                .correctAnswers(test.getCorrectAnswers())
                .scorePercentage(scorePercentage)
                .recommendedLevel(recommendedLevel)
                .feedback(feedback)
                .build();
    }

    private double calculateScore(DiagnosticTest test) {
        return (double) test.getCorrectAnswers() / test.getQuestions().size() * 100;
    }

    private String determineLevel(double score) {
        if (score >= 80) {
            return "A2";
        } else if (score >= 50) {
            return "A1";
        } else {
            return "A1-BEGINNER";
        }
    }
}