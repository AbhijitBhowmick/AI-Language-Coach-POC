package com.coach.common.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NativeLanguageRepository extends JpaRepository<NativeLanguage, UUID> {
    
    Optional<NativeLanguage> findByLanguageCode(String code);
    
    List<NativeLanguage> findByEnabledTrue();
    
    boolean existsByLanguageCode(String code);
}