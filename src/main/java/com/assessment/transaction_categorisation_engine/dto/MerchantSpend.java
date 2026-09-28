package com.assessment.transaction_categorisation_engine.dto;

import java.math.BigDecimal;

public record MerchantSpend(String merchantName, BigDecimal totalSpend) {
}
