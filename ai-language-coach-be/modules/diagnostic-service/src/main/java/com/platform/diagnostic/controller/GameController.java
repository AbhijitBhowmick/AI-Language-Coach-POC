package com.platform.diagnostic.controller;

import com.platform.diagnostic.dto.*;
import com.platform.diagnostic.entity.GameTemplateEntity;
import com.platform.diagnostic.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnostic/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameService.GameSession> startGame(
            @RequestParam String userId,
            @RequestParam String templateId,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false, defaultValue = "A1") String targetLevel) {
        
        GameService.GameSession session = gameService.startGame(userId, templateId, targetLanguage, targetLevel);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<GameRenderData> getCurrentQuestion(@PathVariable String sessionId) {
        GameRenderData renderData = gameService.getCurrentQuestion(sessionId);
        if (renderData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(renderData);
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<GameResult> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody GameAnswer answer) {
        
        GameResult result = gameService.submitAnswer(sessionId, answer);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<GameTemplateEntity>> getTemplates() {
        return ResponseEntity.ok(gameService.getAvailableTemplates());
    }

    @GetMapping("/templates/{category}")
    public ResponseEntity<List<GameTemplateEntity>> getTemplatesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(gameService.getTemplatesByCategory(category.toUpperCase()));
    }

    @PostMapping("/admin/templates")
    public ResponseEntity<GameTemplateEntity> createTemplate(@RequestBody GameTemplateEntity template) {
        GameTemplateEntity saved = gameService.createTemplate(template);
        return ResponseEntity.ok(saved);
    }
}