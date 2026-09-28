package com.assessment.transaction_categorisation_engine.dto;

import java.math.BigDecimal;

public record MonthOverMonthDelta(
        String currentMonth,
        BigDecimal currentMonthSpend,
        String previousMonth,
        BigDecimal previousMonthSpend,
        BigDecimal absoluteDelta,
        BigDecimal percentDelta
) {
}
