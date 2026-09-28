package com.assessment.transaction_categorisation_engine.dto;

import java.math.BigDecimal;

public record CategorySpend(String category, BigDecimal totalSpend) {
}
