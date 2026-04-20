package com.coach.common.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    Optional<SystemConfig> findByConfigKeyAndActiveTrue(String configKey);
    
    @Query("SELECT s FROM SystemConfig s WHERE s.active = true")
    List<SystemConfig> findAllActive();
    
    @Query("SELECT s.configValue FROM SystemConfig s WHERE s.configKey = :key AND s.active = true")
    Optional<String> getValue(String key);
}