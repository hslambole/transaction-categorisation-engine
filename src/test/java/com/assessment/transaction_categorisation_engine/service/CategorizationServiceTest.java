package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.config.CategorizationProperties;
import com.assessment.transaction_categorisation_engine.domain.MerchantCategory;
import com.assessment.transaction_categorisation_engine.domain.StrategyUsed;
import com.assessment.transaction_categorisation_engine.repository.MerchantCategoryRepository;
import com.assessment.transaction_categorisation_engine.repository.TransactionRepository;
import com.assessment.transaction_categorisation_engine.strategy.CategorizationResult;
import com.assessment.transaction_categorisation_engine.strategy.FallbackStrategy;
import com.assessment.transaction_categorisation_engine.strategy.RuleBasedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategorizationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MerchantCategoryRepository merchantCategoryRepository;
    @Mock
    private RuleBasedStrategy ruleBasedStrategy;
    @Mock
    private FallbackStrategy fallbackStrategy;

    private CategorizationProperties properties;
    private CategorizationService service;

    @BeforeEach
    void setUp() {
        properties = new CategorizationProperties();
        properties.getFallback().setEnabled(true);
        service = new CategorizationService(
                transactionRepository,
                merchantCategoryRepository,
                ruleBasedStrategy,
                fallbackStrategy,
                properties
        );
    }

    @Test
    void usesRuleBasedStrategyWhenItMatches() {
        when(merchantCategoryRepository.existsByMerchantName("STARBUCKS PUNE")).thenReturn(false);
        when(ruleBasedStrategy.categorise("STARBUCKS PUNE")).thenReturn(Optional.of(
                new CategorizationResult("Food", "Coffee", new BigDecimal("1.000"), StrategyUsed.RULE_BASED)
        ));
        when(merchantCategoryRepository.save(any(MerchantCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<MerchantCategory> result = service.categoriseMerchant("STARBUCKS PUNE");

        assertThat(result).isPresent();
        assertThat(result.get().getCategory()).isEqualTo("Food");
        assertThat(result.get().getSubCategory()).isEqualTo("Coffee");
        assertThat(result.get().getStrategyUsed()).isEqualTo(StrategyUsed.RULE_BASED);
        assertThat(result.get().getConfidenceScore()).isEqualByComparingTo("1.000");
        verify(fallbackStrategy, never()).categorise(any());
    }

    @Test
    void usesFallbackStrategyWhenNoRuleMatchesAndFallbackEnabled() {
        when(merchantCategoryRepository.existsByMerchantName("UNKNOWN MART")).thenReturn(false);
        when(ruleBasedStrategy.categorise("UNKNOWN MART")).thenReturn(Optional.empty());
        when(fallbackStrategy.categorise("UNKNOWN MART")).thenReturn(Optional.of(
                new CategorizationResult("Shopping", "Grocery", new BigDecimal("0.720"), StrategyUsed.FALLBACK)
        ));
        when(merchantCategoryRepository.save(any(MerchantCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<MerchantCategory> result = service.categoriseMerchant("UNKNOWN MART");

        assertThat(result).isPresent();
        assertThat(result.get().getStrategyUsed()).isEqualTo(StrategyUsed.FALLBACK);
        assertThat(result.get().getConfidenceScore()).isEqualByComparingTo("0.720");

        ArgumentCaptor<MerchantCategory> captor = ArgumentCaptor.forClass(MerchantCategory.class);
        verify(merchantCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("Shopping");
    }

    @Test
    void skipsFallbackWhenDisabled() {
        properties.getFallback().setEnabled(false);
        when(merchantCategoryRepository.existsByMerchantName("UNKNOWN MART")).thenReturn(false);
        when(ruleBasedStrategy.categorise("UNKNOWN MART")).thenReturn(Optional.empty());

        Optional<MerchantCategory> result = service.categoriseMerchant("UNKNOWN MART");

        assertThat(result).isEmpty();
        verify(fallbackStrategy, never()).categorise(any());
        verify(merchantCategoryRepository, never()).save(any());
    }

    @Test
    void skipsEmptyMerchantName() {
        assertThat(service.categoriseMerchant("")).isEmpty();
        assertThat(service.categoriseMerchant("   ")).isEmpty();
        assertThat(service.categoriseMerchant(null)).isEmpty();
        verify(ruleBasedStrategy, never()).categorise(any());
    }

    @Test
    void categorisesAllUncategorisedMerchants() {
        when(transactionRepository.findUncategorisedMerchantNames())
                .thenReturn(List.of("STARBUCKS", "UNKNOWN MART"));
        when(merchantCategoryRepository.existsByMerchantName(any())).thenReturn(false);
        when(ruleBasedStrategy.categorise("STARBUCKS")).thenReturn(Optional.of(
                new CategorizationResult("Food", "Coffee", new BigDecimal("1.000"), StrategyUsed.RULE_BASED)
        ));
        when(ruleBasedStrategy.categorise("UNKNOWN MART")).thenReturn(Optional.empty());
        when(fallbackStrategy.categorise("UNKNOWN MART")).thenReturn(Optional.of(
                new CategorizationResult("Other", "Other", new BigDecimal("0.400"), StrategyUsed.FALLBACK)
        ));
        when(merchantCategoryRepository.save(any(MerchantCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int saved = service.categoriseUncategorisedMerchants();

        assertThat(saved).isEqualTo(2);
    }
}
