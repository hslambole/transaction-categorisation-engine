package com.assessment.transaction_categorisation_engine.client;

public interface LlmClient {

    LlmCategoryResponse classify(String merchantName);
}
