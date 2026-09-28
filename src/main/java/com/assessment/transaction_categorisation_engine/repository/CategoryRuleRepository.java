package com.assessment.transaction_categorisation_engine.repository;

import com.assessment.transaction_categorisation_engine.domain.CategoryRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {

    List<CategoryRule> findByIsActiveTrueOrderByPriorityDesc();

    Optional<CategoryRule> findByKeywordIgnoreCase(String keyword);
}
