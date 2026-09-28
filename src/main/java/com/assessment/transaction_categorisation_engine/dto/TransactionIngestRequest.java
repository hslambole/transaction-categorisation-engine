package com.assessment.transaction_categorisation_engine.dto;

import com.assessment.transaction_categorisation_engine.domain.Source;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;


public record TransactionIngestRequest(
        @NotBlank(message = "merchantName must not be blank")
        String merchantName,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
        String currency,

        @NotNull(message = "transactionDate is required")
        LocalDate transactionDate,

        @NotNull(message = "source is required")
        Source source
) {
}
