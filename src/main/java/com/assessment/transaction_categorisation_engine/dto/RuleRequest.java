package com.assessment.transaction_categorisation_engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleRequest(
        @NotBlank(message = "keyword must not be blank")
        String keyword,

        @NotBlank(message = "category must not be blank")
        String category,

        @NotBlank(message = "subCategory must not be blank")
        String subCategory,

        @NotNull(message = "priority is required")
        Integer priority,

        Boolean isActive
) {
}
