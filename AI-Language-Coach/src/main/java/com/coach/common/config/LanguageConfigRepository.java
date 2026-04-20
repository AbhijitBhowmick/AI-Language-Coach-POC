package com.coach.common.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LanguageConfigRepository extends JpaRepository<LanguageConfig, UUID> {
    
    Optional<LanguageConfig> findByLanguageCodeAndLevel(String languageCode, String level);
    
    List<LanguageConfig> findByEnabledTrueOrderByDisplayOrder();
    
    List<LanguageConfig> findByLanguageCodeAndEnabledTrue(String languageCode);
    
    boolean existsByLanguageCodeAndLevel(String languageCode, String level);
}