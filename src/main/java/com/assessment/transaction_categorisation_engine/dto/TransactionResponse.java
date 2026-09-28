package com.assessment.transaction_categorisation_engine.dto;

import com.assessment.transaction_categorisation_engine.domain.Source;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String merchantName,
        BigDecimal amount,
        String currency,
        LocalDate transactionDate,
        Source source,
        Instant createdAt,
        String category,
        String subCategory
) {
}
