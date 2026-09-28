package com.assessment.transaction_categorisation_engine.service;

import com.assessment.transaction_categorisation_engine.domain.Source;
import com.assessment.transaction_categorisation_engine.domain.Transaction;
import com.assessment.transaction_categorisation_engine.dto.BulkIngestResponse;
import com.assessment.transaction_categorisation_engine.dto.TransactionIngestRequest;
import com.assessment.transaction_categorisation_engine.repository.MerchantCategoryRepository;
import com.assessment.transaction_categorisation_engine.repository.TransactionRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MerchantCategoryRepository merchantCategoryRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        transactionService = new TransactionService(
                transactionRepository,
                merchantCategoryRepository,
                validator,
                new CurrencyValidator()
        );
    }

    @Test
    void acceptsValidTransaction() {
        TransactionIngestRequest request = validRequest("STARBUCKS PUNE", "INR");
        when(transactionRepository.existsByMerchantNameAndAmountAndCurrencyAndTransactionDateAndSource(
                any(), any(), any(), any(), any()
        )).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BulkIngestResponse response = transactionService.ingest(List.of(request));

        assertThat(response.accepted()).isEqualTo(1);
        assertThat(response.rejected()).isZero();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void rejectsEmptyMerchantName() {
        TransactionIngestRequest request = new TransactionIngestRequest(
                "  ",
                new BigDecimal("10.00"),
                "INR",
                LocalDate.of(2026, 9, 20),
                Source.BANK_FEED
        );

        BulkIngestResponse response = transactionService.ingest(List.of(request));

        assertThat(response.accepted()).isZero();
        assertThat(response.rejected()).isEqualTo(1);
        assertThat(response.errors().getFirst().message()).contains("merchantName");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownCurrency() {
        TransactionIngestRequest request = validRequest("STARBUCKS PUNE", "ZZZ");

        BulkIngestResponse response = transactionService.ingest(List.of(request));

        assertThat(response.accepted()).isZero();
        assertThat(response.rejected()).isEqualTo(1);
        assertThat(response.errors().getFirst().message()).isEqualTo("unknown currency");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateIngestion() {
        TransactionIngestRequest request = validRequest("STARBUCKS PUNE", "INR");
        when(transactionRepository.existsByMerchantNameAndAmountAndCurrencyAndTransactionDateAndSource(
                "STARBUCKS PUNE",
                request.amount(),
                "INR",
                request.transactionDate(),
                Source.BANK_FEED
        )).thenReturn(true);

        BulkIngestResponse response = transactionService.ingest(List.of(request));

        assertThat(response.accepted()).isZero();
        assertThat(response.rejected()).isEqualTo(1);
        assertThat(response.errors().getFirst().message()).isEqualTo("duplicate transaction");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void mixedBatchAcceptsValidAndRejectsInvalid() {
        when(transactionRepository.existsByMerchantNameAndAmountAndCurrencyAndTransactionDateAndSource(
                any(), any(), any(), any(), any()
        )).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BulkIngestResponse response = transactionService.ingest(List.of(
                validRequest("STARBUCKS", "INR"),
                validRequest("AMAZON", "ZZZ"),
                new TransactionIngestRequest("", new BigDecimal("5.00"), "USD", LocalDate.now(), Source.MANUAL)
        ));

        assertThat(response.accepted()).isEqualTo(1);
        assertThat(response.rejected()).isEqualTo(2);
        verify(transactionRepository, times(1)).save(any());
    }

    private TransactionIngestRequest validRequest(String merchant, String currency) {
        return new TransactionIngestRequest(
                merchant,
                new BigDecimal("420.50"),
                currency,
                LocalDate.of(2026, 9, 20),
                Source.BANK_FEED
        );
    }
}
