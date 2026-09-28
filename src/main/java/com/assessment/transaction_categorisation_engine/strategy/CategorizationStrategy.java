package com.assessment.transaction_categorisation_engine.strategy;

import java.util.Optional;

public interface CategorizationStrategy {

    Optional<CategorizationResult> categorise(String merchantName);
}
