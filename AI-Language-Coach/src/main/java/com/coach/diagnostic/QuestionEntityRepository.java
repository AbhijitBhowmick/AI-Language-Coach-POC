package com.coach.diagnostic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionEntityRepository extends JpaRepository<QuestionEntity, UUID> {
    
    @Query("SELECT q FROM QuestionEntity q WHERE q.targetLanguage = :lang AND q.targetLevel = :level AND q.active = true ORDER BY q.displayOrder")
    List<QuestionEntity> findActiveQuestions(String lang, String level);
    
    @Query("SELECT q FROM QuestionEntity q WHERE q.targetLanguage = :lang AND q.targetLevel = :level AND q.nativeLanguage = :nativeLang AND q.active = true ORDER BY q.displayOrder")
    List<QuestionEntity> findActiveQuestionsForNative(String lang, String level, String nativeLang);
    
    @Query("SELECT DISTINCT q.targetLanguage FROM QuestionEntity q WHERE q.active = true")
    List<String> findDistinctLanguages();
    
    @Query("SELECT DISTINCT q.targetLevel FROM QuestionEntity q WHERE q.targetLanguage = :lang AND q.active = true")
    List<String> findLevelsForLanguage(String lang);
    
    long countByTargetLanguageAndTargetLevelAndActive(String language, String level, boolean active);
}