package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;

import java.util.*;

public class GroupSortGameEngine implements GameEngine {

    private final GameTemplateFactory factory;

    public GroupSortGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "group_sort";
    }

    @Override
    public GameType getGameType() {
        return GameType.GROUP_SORT;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        List<GameQuestion.CategoryItem> items = question.categoryItems();
        List<GameRenderData.CategoryData> categories = new ArrayList<>();
        
        Set<String> categoryNames = new LinkedHashSet<>();
        if (items != null) {
            for (GameQuestion.CategoryItem item : items) {
                categoryNames.add(item.category());
            }
        }

        for (String catName : categoryNames) {
            List<GameRenderData.CategoryItem> catItems = new ArrayList<>();
            if (items != null) {
                for (GameQuestion.CategoryItem item : items) {
                    if (catName.equals(item.category())) {
                        catItems.add(new GameRenderData.CategoryItem(item.id(), item.text(), item.imageUrl(), catName));
                    }
                }
            }
            categories.add(new GameRenderData.CategoryData(catName.toLowerCase().replaceAll("\\s+", "_"), catName, catItems));
        }

        List<GameRenderData.SortItem> sortItems = new ArrayList<>();
        if (items != null) {
            List<GameRenderData.SortItem> shuffled = new ArrayList<>();
            for (GameQuestion.CategoryItem item : items) {
                shuffled.add(new GameRenderData.SortItem(item.id(), item.text(), item.category()));
            }
            Collections.shuffle(shuffled);
            sortItems.addAll(shuffled);
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Group Sort")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Drag items to the correct category")
            .questionText(question.questionText())
            .categories(categories)
            .sortItems(sortItems)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        List<String> sorted = answer.sortedCategories();
        List<GameQuestion.CategoryItem> items = question.categoryItems();
        
        if (sorted == null || items == null) {
            return GameResult.builder().correct(false).pointsEarned(0).build();
        }

        int correct = 0;
        for (int i = 0; i < Math.min(sorted.size(), items.size()); i++) {
            String userCategory = sorted.get(i);
            String correctCategory = items.get(i).category();
            if (userCategory.equalsIgnoreCase(correctCategory)) {
                correct++;
            }
        }

        boolean allCorrect = correct == items.size();
        int basePoints = question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId());
        int earnedPoints = allCorrect ? basePoints : (correct * (basePoints / Math.max(1, items.size())));

        return GameResult.builder()
            .correct(allCorrect)
            .pointsEarned(earnedPoints)
            .explanation(question.explanation())
            .linguisticBridge(question.linguisticBridge())
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.sortedCategories() == null || answer.sortedCategories().isEmpty()) {
            return ValidationResult.failure("No items sorted");
        }
        return ValidationResult.success();
    }

    @Override
    public int getDefaultTimeSeconds() {
        return factory.getDefaultTimeSeconds(getTemplateId());
    }

    @Override
    public boolean supportsLives() {
        return false;
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }
}