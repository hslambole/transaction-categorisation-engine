package com.assessment.transaction_categorisation_engine.strategy;

import com.assessment.transaction_categorisation_engine.domain.StrategyUsed;

import java.math.BigDecimal;

public record CategorizationResult(
        String category,
        String subCategory,
        BigDecimal confidenceScore,
        StrategyUsed strategyUsed
) {
}
