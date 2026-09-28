package com.assessment.transaction_categorisation_engine.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmCategoryResponse(
        String category,
        @JsonAlias("sub_category") String subCategory,
        BigDecimal confidence
) {
}
