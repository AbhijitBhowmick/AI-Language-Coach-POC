package com.coach.common.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final LanguageConfigRepository languageRepository;
    private final com.coach.diagnostic.QuestionEntityRepository questionRepository;

    public ConfigController(LanguageConfigRepository languageRepository, 
                           com.coach.diagnostic.QuestionEntityRepository questionRepository) {
        this.languageRepository = languageRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageConfig>> getLanguages() {
        return ResponseEntity.ok(languageRepository.findByEnabledTrueOrderByDisplayOrder());
    }

    @GetMapping("/languages/{code}/levels")
    public ResponseEntity<List<String>> getLevelsForLanguage(@PathVariable String code) {
        return ResponseEntity.ok(
            languageRepository.findByLanguageCodeAndEnabledTrue(code)
                .stream()
                .map(LanguageConfig::getLevel)
                .toList()
        );
    }

    @PostMapping("/languages")
    public ResponseEntity<LanguageConfig> createLanguage(@RequestBody LanguageConfig config) {
        return ResponseEntity.ok(languageRepository.save(config));
    }

    @GetMapping("/questions/languages")
    public ResponseEntity<List<String>> getQuestionLanguages() {
        return ResponseEntity.ok(questionRepository.findDistinctLanguages());
    }

    @GetMapping("/questions/{language}/levels")
    public ResponseEntity<List<String>> getQuestionLevels(@PathVariable String language) {
        return ResponseEntity.ok(questionRepository.findLevelsForLanguage(language));
    }
}