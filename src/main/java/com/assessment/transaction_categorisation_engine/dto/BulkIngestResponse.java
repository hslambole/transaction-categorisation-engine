package com.assessment.transaction_categorisation_engine.dto;

import java.util.List;

public record BulkIngestResponse(
        int accepted,
        int rejected,
        List<RejectedTransaction> errors
) {
}
