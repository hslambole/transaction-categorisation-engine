package com.assessment.transaction_categorisation_engine.dto;

import java.util.List;

public record AnalyticsSummaryResponse(
        List<CategorySpend> spendByCategory,
        List<MerchantSpend> topMerchants,
        MonthOverMonthDelta monthOverMonth
) {
}
