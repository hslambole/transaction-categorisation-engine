package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.config.CategorizationProperties;
import com.assessment.transaction_categorisation_engine.domain.MerchantCategory;
import com.assessment.transaction_categorisation_engine.repository.MerchantCategoryRepository;
import com.assessment.transaction_categorisation_engine.repository.TransactionRepository;
import com.assessment.transaction_categorisation_engine.strategy.CategorizationResult;
import com.assessment.transaction_categorisation_engine.strategy.FallbackStrategy;
import com.assessment.transaction_categorisation_engine.strategy.RuleBasedStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategorizationService {

    private static final Logger log = LoggerFactory.getLogger(CategorizationService.class);

    private final TransactionRepository transactionRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final RuleBasedStrategy ruleBasedStrategy;
    private final FallbackStrategy fallbackStrategy;
    private final CategorizationProperties properties;

    public CategorizationService(
            TransactionRepository transactionRepository,
            MerchantCategoryRepository merchantCategoryRepository,
            RuleBasedStrategy ruleBasedStrategy,
            FallbackStrategy fallbackStrategy,
            CategorizationProperties properties
    ) {
        this.transactionRepository = transactionRepository;
        this.merchantCategoryRepository = merchantCategoryRepository;
        this.ruleBasedStrategy = ruleBasedStrategy;
        this.fallbackStrategy = fallbackStrategy;
        this.properties = properties;
    }

    @Async("categorizationExecutor")
    public void triggerAsync() {
        categoriseUncategorisedMerchants();
    }

    @Transactional
    public int categoriseUncategorisedMerchants() {
        List<String> merchants = transactionRepository.findUncategorisedMerchantNames();
        int saved = 0;
        for (String merchantName : merchants) {
            if (categoriseMerchant(merchantName).isPresent()) {
                saved++;
            }
        }
        log.info("Categorised {} of {} uncategorised merchants", saved, merchants.size());
        return saved;
    }

    public Optional<MerchantCategory> categoriseMerchant(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            return Optional.empty();
        }
        if (merchantCategoryRepository.existsByMerchantName(merchantName)) {
            return merchantCategoryRepository.findByMerchantName(merchantName);
        }

        Optional<CategorizationResult> result = ruleBasedStrategy.categorise(merchantName);
        if (result.isEmpty() && properties.getFallback().isEnabled()) {
            result = fallbackStrategy.categorise(merchantName);
        }

        if (result.isEmpty()) {
            return Optional.empty();
        }

        CategorizationResult matched = result.get();
        MerchantCategory category = new MerchantCategory();
        category.setMerchantName(merchantName);
        category.setCategory(matched.category());
        category.setSubCategory(matched.subCategory());
        category.setConfidenceScore(matched.confidenceScore());
        category.setStrategyUsed(matched.strategyUsed());
        return Optional.of(merchantCategoryRepository.save(category));
    }
}
