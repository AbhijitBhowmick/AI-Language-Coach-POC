package com.platform.diagnostic.game;

import com.platform.diagnostic.entity.GameTemplateEntity;
import com.platform.diagnostic.repository.GameTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameTemplateFactory {

    private static final Logger log = LoggerFactory.getLogger(GameTemplateFactory.class);

    private final GameTemplateRepository templateRepository;
    private final Map<String, GameEngine> gameEngines = new ConcurrentHashMap<>();
    private final Map<String, GameTemplateEntity> templateCache = new ConcurrentHashMap<>();

    public GameTemplateFactory(GameTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    public void initialize() {
        registerBuiltInEngines();
        loadTemplatesFromDatabase();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        loadTemplatesFromDatabase();
    }

    private void loadTemplatesFromDatabase() {
        try {
            List<GameTemplateEntity> templates = templateRepository.findByActiveTrueOrderByDisplayOrder();
            for (GameTemplateEntity template : templates) {
                templateCache.put(template.getTemplateId(), template);
                log.info("Loaded game template: {} - {}", template.getTemplateId(), template.getDisplayName());
            }
            log.info("Loaded {} game templates from database", templates.size());
        } catch (Exception e) {
            log.error("Failed to load game templates from database: {}", e.getMessage());
        }
    }

    private void registerBuiltInEngines() {
        registerEngine(new StandardGameEngine(this));
        registerEngine(new MatchUpGameEngine(this));
        registerEngine(new ClozeTextGameEngine(this));
        registerEngine(new AnagramGameEngine(this));
        registerEngine(new WhackAMoleGameEngine(this));
        registerEngine(new SpeakingCardGameEngine(this));
        registerEngine(new BranchingScenarioEngine(this));
        registerEngine(new GroupSortGameEngine(this));
    }

    public void registerEngine(GameEngine engine) {
        gameEngines.put(engine.getTemplateId(), engine);
        log.info("Registered game engine: {} for template: {}", engine.getGameType(), engine.getTemplateId());
    }

    public GameEngine getEngine(String templateId) {
        GameEngine engine = gameEngines.get(templateId);
        if (engine == null) {
            log.warn("No engine found for template: {}, using standard engine", templateId);
            return gameEngines.get("standard");
        }
        return engine;
    }

    public GameTemplateEntity getTemplate(String templateId) {
        return templateCache.get(templateId);
    }

    public List<GameTemplateEntity> getAllTemplates() {
        return List.copyOf(templateCache.values());
    }

    public List<GameTemplateEntity> getTemplatesByCategory(String category) {
        return templateCache.values().stream()
            .filter(t -> t.getTemplateCategory().equalsIgnoreCase(category))
            .toList();
    }

    public void reloadTemplates() {
        log.info("Reloading game templates from database");
        templateCache.clear();
        loadTemplatesFromDatabase();
    }

    public int getDefaultTimeSeconds(String templateId) {
        GameTemplateEntity template = templateCache.get(templateId);
        return template != null && template.getDefaultTimeSeconds() != null ? template.getDefaultTimeSeconds() : 30;
    }

    public int getPointsPerCorrect(String templateId) {
        GameTemplateEntity template = templateCache.get(templateId);
        return template != null && template.getPointsPerCorrect() != null ? template.getPointsPerCorrect() : 10;
    }

    public int getPenaltyPoints(String templateId) {
        GameTemplateEntity template = templateCache.get(templateId);
        return template != null && template.getPenaltyPoints() != null ? template.getPenaltyPoints() : 0;
    }

    public boolean supportsLives(String templateId) {
        GameTemplateEntity template = templateCache.get(templateId);
        return template != null && Boolean.TRUE.equals(template.getLivesEnabled());
    }

    public boolean supportsBranching(String templateId) {
        GameTemplateEntity template = templateCache.get(templateId);
        return template != null && Boolean.TRUE.equals(template.getBranchingEnabled());
    }
}