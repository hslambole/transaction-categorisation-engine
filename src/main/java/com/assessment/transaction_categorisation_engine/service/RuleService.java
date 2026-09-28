package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.domain.CategoryRule;
import com.assessment.transaction_categorisation_engine.dto.RuleRequest;
import com.assessment.transaction_categorisation_engine.dto.RuleResponse;
import com.assessment.transaction_categorisation_engine.repository.CategoryRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {

    private final CategoryRuleRepository categoryRuleRepository;

    public RuleService(CategoryRuleRepository categoryRuleRepository) {
        this.categoryRuleRepository = categoryRuleRepository;
    }

    @Transactional
    public RuleResponse createOrUpdate(RuleRequest request) {
        String keyword = request.keyword().trim().toLowerCase();
        CategoryRule rule = categoryRuleRepository.findByKeywordIgnoreCase(keyword)
                .orElseGet(CategoryRule::new);

        rule.setKeyword(keyword);
        rule.setCategory(request.category().trim());
        rule.setSubCategory(request.subCategory().trim());
        rule.setPriority(request.priority());
        rule.setIsActive(request.isActive() == null || request.isActive());

        CategoryRule saved = categoryRuleRepository.save(rule);
        return new RuleResponse(
                saved.getId(),
                saved.getKeyword(),
                saved.getCategory(),
                saved.getSubCategory(),
                saved.getPriority(),
                saved.getIsActive()
        );
    }
}
