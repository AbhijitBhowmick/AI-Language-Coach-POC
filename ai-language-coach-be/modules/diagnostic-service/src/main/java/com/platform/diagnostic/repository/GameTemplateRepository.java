package com.platform.diagnostic.repository;

import com.platform.diagnostic.entity.GameTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameTemplateRepository extends JpaRepository<GameTemplateEntity, String> {

    List<GameTemplateEntity> findByActiveTrueOrderByDisplayOrder();

    List<GameTemplateEntity> findByTemplateCategoryAndActiveTrue(String category);

    List<GameTemplateEntity> findByActiveTrue();
}