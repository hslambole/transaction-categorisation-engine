package com.assessment.transaction_categorisation_engine.dto;

public record RuleResponse(
        Long id,
        String keyword,
        String category,
        String subCategory,
        Integer priority,
        Boolean isActive
) {
}
