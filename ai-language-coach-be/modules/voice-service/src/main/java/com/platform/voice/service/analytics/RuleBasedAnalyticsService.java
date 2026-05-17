package com.platform.voice.service.analytics;

import com.platform.voice.config.AnalyticsProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;
import com.platform.voice.model.AnalysisResult.CEFRAlignment;
import com.platform.voice.model.AnalysisResult.IdentifiedMistake;
import com.platform.voice.model.AnalysisResult.ProgressMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class RuleBasedAnalyticsService implements AnalyticsStrategy {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedAnalyticsService.class);

    private final Counter ruleBasedAnalysisCounter;
    
    private static final Map<String, String> COMMON_MISTAKES;
    private static final Map<String, Integer> A1_KEYWORDS;
    private static final Map<String, Integer> A2_KEYWORDS;

    private static final Pattern PRONOUN_PATTERN = Pattern.compile(
        "(já|ty|on|ona|ono|my|vy|oni|ony|se|sobě|si|mě|mně)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern VERB_PATTERN = Pattern.compile(
        "(jsem|jssi|je|jsme|jste|jsou|byl|byla|bylo|mám|máš|máme|máte|mají|chci|chceš|chceme|chcete|chtěl|chtěla)",
        Pattern.CASE_INSENSITIVE
    );

    static {
        COMMON_MISTAKES = new HashMap<>();
        COMMON_MISTAKES.put("já jsem", "I am - use 'jsem' not 'sou'");
        COMMON_MISTAKES.put("jsem jsem", "Redundant 'jsem'");
        COMMON_MISTAKES.put("mám jít", "Going - use 'jdu' not 'jít'");
        COMMON_MISTAKES.put("mám vidět", "Seeing - use 'vidím' not 'vidět'");
        COMMON_MISTAKES.put("mohu jsem", "Can I - use 'mohu' correctly");

        A1_KEYWORDS = new HashMap<>();
        A1_KEYWORDS.put("ahoj", 1);
        A1_KEYWORDS.put("dobrý", 1);
        A1_KEYWORDS.put("den", 1);
        A1_KEYWORDS.put("děkuji", 1);
        A1_KEYWORDS.put("prosím", 1);
        A1_KEYWORDS.put("ano", 1);
        A1_KEYWORDS.put("ne", 1);
        A1_KEYWORDS.put("mám", 1);
        A1_KEYWORDS.put("jmenuji", 1);
        A1_KEYWORDS.put("bydlím", 1);
        A1_KEYWORDS.put("rodina", 1);
        A1_KEYWORDS.put("matka", 1);
        A1_KEYWORDS.put("otec", 1);
        A1_KEYWORDS.put("bratr", 1);
        A1_KEYWORDS.put("sestra", 1);
        A1_KEYWORDS.put("číslo", 1);
        A1_KEYWORDS.put("adresa", 1);
        A1_KEYWORDS.put("telefon", 1);
        A1_KEYWORDS.put("kde", 1);
        A1_KEYWORDS.put("kdy", 1);

        A2_KEYWORDS = new HashMap<>();
        A2_KEYWORDS.put("potřebuji", 2);
        A2_KEYWORDS.put("mohu", 2);
        A2_KEYWORDS.put("chtěl", 2);
        A2_KEYWORDS.put("potřeboval", 2);
        A2_KEYWORDS.put("nákup", 2);
        A2_KEYWORDS.put("jídlo", 2);
        A2_KEYWORDS.put("restaurace", 2);
        A2_KEYWORDS.put("obchod", 2);
        A2_KEYWORDS.put("lékař", 2);
        A2_KEYWORDS.put("hospoda", 2);
        A2_KEYWORDS.put("vlak", 2);
        A2_KEYWORDS.put("autobus", 2);
        A2_KEYWORDS.put("zapisování", 2);
        A2_KEYWORDS.put("schůzka", 2);
        A2_KEYWORDS.put("práce", 2);
    }

    public RuleBasedAnalyticsService(MeterRegistry meterRegistry) {
        this.ruleBasedAnalysisCounter = Counter.builder("voice.analytics.rulebased.requests")
                .description("Number of rule-based analytics requests")
                .register(meterRegistry);
    }

    @Override
    public AnalysisResult analyze(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        ruleBasedAnalysisCounter.increment();
        
        log.debug("Running rule-based analysis for user: {}, transcript: {}", 
            context.userId(), transcript);
        
        List<IdentifiedMistake> mistakes = analyzeMistakes(transcript, context.targetLanguage());
        ProgressMetrics progress = calculateProgress(transcript, context.targetLevel(), audioDurationMs);
        CEFRAlignment cefrAlignment = assessCEFLevel(transcript, context.targetLanguage());
        List<String> recommendations = generateRecommendations(mistakes, cefrAlignment);
        
        return new AnalysisResult(
            UUID.randomUUID().toString(),
            "rule-based",
            context.planType(),
            transcript,
            mistakes,
            progress,
            cefrAlignment,
            recommendations,
            Instant.now(),
            "rule-based-v1"
        );
    }

    private List<IdentifiedMistake> analyzeMistakes(String transcript, String targetLanguage) {
        List<IdentifiedMistake> mistakes = new ArrayList<>();
        
        if (transcript == null || transcript.isBlank()) {
            return mistakes;
        }
        
        for (Map.Entry<String, String> entry : COMMON_MISTAKES.entrySet()) {
            if (transcript.toLowerCase().contains(entry.getKey())) {
                mistakes.add(new IdentifiedMistake(
                    "common_error",
                    entry.getValue(),
                    "medium",
                    entry.getKey(),
                    entry.getValue(),
                    "grammar"
                ));
            }
        }
        
        if (!PRONOUN_PATTERN.matcher(transcript).find()) {
            mistakes.add(new IdentifiedMistake(
                "missing_pronoun",
                "No pronouns found - sentences may be incomplete",
                "low",
                "",
                "Include pronouns like 'já', 'ty', 'my'",
                "syntax"
            ));
        }
        
        if (!VERB_PATTERN.matcher(transcript).find()) {
            mistakes.add(new IdentifiedMistake(
                "missing_verb",
                "No verbs found - sentence structure may be incorrect",
                "medium",
                "",
                "Include verbs like 'jsem', 'mám', 'chci'",
                "syntax"
            ));
        }
        
        if (transcript.length() < 5 && !transcript.contains(" ")) {
            mistakes.add(new IdentifiedMistake(
                "too_short",
                "Very short response - try to form complete sentences",
                "low",
                transcript,
                "Add more words to form sentences",
                "comprehension"
            ));
        }
        
        return mistakes;
    }

    private ProgressMetrics calculateProgress(String transcript, String targetLevel, long audioDurationMs) {
        Map<String, Long> topicFreq = new HashMap<>();
        
        if (transcript != null) {
            String[] words = transcript.toLowerCase().split("\\s+");
            for (String word : words) {
                if (A1_KEYWORDS.containsKey(word)) {
                    topicFreq.merge("survival", 1L, Long::sum);
                }
                if (A2_KEYWORDS.containsKey(word)) {
                    topicFreq.merge("situational", 1L, Long::sum);
                }
            }
        }
        
        double score = transcript != null && transcript.length() > 10 ? 70.0 : 50.0;
        
        return new ProgressMetrics(
            score,
            score > 60 ? 5.0 : 0.0,
            1L,
            audioDurationMs,
            topicFreq
        );
    }

    private CEFRAlignment assessCEFLevel(String transcript, String targetLanguage) {
        int a1Score = 0;
        int a2Score = 0;
        
        if (transcript != null) {
            String[] words = transcript.toLowerCase().split("\\s+");
            for (String word : words) {
                if (A1_KEYWORDS.containsKey(word)) a1Score++;
                if (A2_KEYWORDS.containsKey(word)) a2Score++;
            }
        }
        
        int total = transcript != null ? transcript.split("\\s+").length : 0;
        double a1Ratio = total > 0 ? (double) a1Score / total * 100 : 0;
        double a2Ratio = total > 0 ? (double) a2Score / total * 100 : 0;
        
        String suggestedLevel = a2Ratio > 20 ? "A2" : "A1";
        String assessment = String.format(
            "A1 keywords: %d (%.1f%%), A2 keywords: %d (%.1f%%)",
            a1Score, a1Ratio, a2Score, a2Ratio
        );
        
        return new CEFRAlignment(suggestedLevel, a1Score, a2Score, 0, assessment);
    }

    private List<String> generateRecommendations(List<IdentifiedMistake> mistakes, CEFRAlignment cefr) {
        List<String> recs = new ArrayList<>();
        
        if (!mistakes.isEmpty()) {
            recs.add("Practice common Czech phrases daily");
            recs.add("Focus on pronoun-verb structure: 'já jsem', 'ty jsi'");
        }
        
        if (cefr.suggestedLevel().equals("A2")) {
            recs.add("You're ready for A2 topics - try ordering food, making reservations");
        } else {
            recs.add("Keep practicing A1 basics - introduce yourself, give phone number");
        }
        
        recs.add("Listen to Czech radio for exposure");
        
        return recs;
    }

    @Override
    public String getAnalyticsType() {
        return "rule-based";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return "STANDARD".equalsIgnoreCase(planType);
    }
}