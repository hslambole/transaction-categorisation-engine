package com.assessment.transaction_categorisation_engine.strategy;

import com.assessment.transaction_categorisation_engine.domain.CategoryRule;
import com.assessment.transaction_categorisation_engine.domain.StrategyUsed;
import com.assessment.transaction_categorisation_engine.repository.CategoryRuleRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class RuleBasedStrategy implements CategorizationStrategy {

    public static final BigDecimal RULE_CONFIDENCE = new BigDecimal("1.000");

    private final CategoryRuleRepository categoryRuleRepository;

    public RuleBasedStrategy(CategoryRuleRepository categoryRuleRepository) {
        this.categoryRuleRepository = categoryRuleRepository;
    }

    @Override
    public Optional<CategorizationResult> categorise(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            return Optional.empty();
        }

        String haystack = merchantName.toLowerCase();
        List<CategoryRule> rules = categoryRuleRepository.findByIsActiveTrueOrderByPriorityDesc();

        return rules.stream()
                .filter(rule -> rule.getKeyword() != null && haystack.contains(rule.getKeyword().toLowerCase()))
                .findFirst()
                .map(rule -> new CategorizationResult(
                        rule.getCategory(),
                        rule.getSubCategory(),
                        RULE_CONFIDENCE,
                        StrategyUsed.RULE_BASED
                ));
    }
}
