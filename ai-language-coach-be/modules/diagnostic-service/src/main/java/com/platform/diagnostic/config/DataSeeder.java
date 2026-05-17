package com.platform.diagnostic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void seedGameTemplates() {
        try {
            jdbcTemplate.update("UPDATE game_templates SET active = TRUE WHERE active IS NULL OR active = FALSE");
            int activeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM game_templates WHERE active = TRUE", Integer.class);
            if (activeCount >= 8) {
                log.info("game_templates already contains {} active rows, skipping seed", activeCount);
                return;
            }
        } catch (Exception e) {
            log.warn("Could not check game_templates: {}", e.getMessage());
        }

        log.info("Seeding game_templates...");
        String sql = """
            INSERT INTO game_templates (template_id, display_name, template_category, display_order, icon_class, description,
                default_time_seconds, points_per_correct, penalty_points, lives_enabled, branching_enabled,
                min_questions, max_questions, skill_areas, active) VALUES
            ('standard', 'Quiz', 'GRAMMAR', 1, 'fa-question-circle', 'Multiple choice questions',
                30, 10, 0, FALSE, FALSE, 5, 20, '["reading","writing"]', TRUE),
            ('match_up', 'Match Up', 'VOCAB', 2, 'fa-puzzle-piece', 'Drag and drop matching pairs',
                60, 15, -5, FALSE, FALSE, 6, 16, '["reading","vocabulary"]', TRUE),
            ('cloze_text', 'Fill in the Blank', 'GRAMMAR', 3, 'fa-pen-fancy', 'Gap-fill text completion',
                45, 12, -3, FALSE, FALSE, 3, 15, '["reading","writing"]', TRUE),
            ('anagram', 'Anagram', 'VOCAB', 4, 'fa-font', 'Unscramble the word or phrase',
                30, 15, -5, TRUE, FALSE, 5, 15, '["spelling"]', TRUE),
            ('whack_mole', 'Whack-a-Mole', 'VOCAB', 5, 'fa-bug', 'Hit the correct answer fast',
                20, 20, -10, TRUE, FALSE, 10, 20, '["listening","recognition"]', TRUE),
            ('speaking_card', 'Speaking Card', 'SPEAKING', 6, 'fa-microphone', 'Record your spoken response',
                90, 25, 0, FALSE, FALSE, 1, 10, '["speaking"]', TRUE),
            ('situational_branching', 'Scenario', 'LISTENING', 7, 'fa-users', 'Branching conversation scenario',
                120, 30, 0, FALSE, TRUE, 3, 10, '["listening","speaking"]', TRUE),
            ('group_sort', 'Group Sort', 'GRAMMAR', 8, 'fa-layer-group', 'Drag items to correct categories',
                60, 15, -5, FALSE, FALSE, 6, 15, '["grammar","vocabulary"]', TRUE)
            ON CONFLICT (template_id) DO UPDATE SET active = TRUE
            """;
        try {
            int rows = jdbcTemplate.update(sql);
            log.info("Seeded {} game templates", rows);
        } catch (Exception e) {
            log.error("Failed to seed game_templates: {}", e.getMessage());
        }
    }
}
