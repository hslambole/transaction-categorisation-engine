package com.assessment.transaction_categorisation_engine.strategy;

import com.assessment.transaction_categorisation_engine.client.LlmCategoryResponse;
import com.assessment.transaction_categorisation_engine.client.LlmClient;
import com.assessment.transaction_categorisation_engine.domain.StrategyUsed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class FallbackStrategy implements CategorizationStrategy {

    private static final Logger log = LoggerFactory.getLogger(FallbackStrategy.class);

    private final LlmClient llmClient;

    public FallbackStrategy(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public Optional<CategorizationResult> categorise(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            return Optional.empty();
        }

        try {
            LlmCategoryResponse response = llmClient.classify(merchantName);
            if (response == null || response.category() == null || response.category().isBlank()) {
                return Optional.empty();
            }

            BigDecimal confidence = response.confidence() == null ? BigDecimal.ZERO : response.confidence();
            if (confidence.compareTo(BigDecimal.ZERO) < 0) {
                confidence = BigDecimal.ZERO;
            } else if (confidence.compareTo(BigDecimal.ONE) > 0) {
                confidence = BigDecimal.ONE;
            }

            String subCategory = response.subCategory() == null || response.subCategory().isBlank()
                    ? "Other"
                    : response.subCategory();

            return Optional.of(new CategorizationResult(
                    response.category(),
                    subCategory,
                    confidence,
                    StrategyUsed.FALLBACK
            ));
        } catch (Exception ex) {
            log.warn("Fallback LLM categorisation failed for merchant '{}': {}", merchantName, ex.getMessage());
            return Optional.empty();
        }
    }
}
