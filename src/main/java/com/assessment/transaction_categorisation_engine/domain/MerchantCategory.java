package com.assessment.transaction_categorisation_engine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "merchant_categories")
public class MerchantCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_name", nullable = false, unique = true)
    private String merchantName;

    @Column(nullable = false)
    private String category;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;

    @Column(name = "confidence_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "categorised_at", nullable = false)
    private Instant categorisedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_used", nullable = false, length = 32)
    private StrategyUsed strategyUsed;

    @PrePersist
    void onCreate() {
        if (categorisedAt == null) {
            categorisedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Instant getCategorisedAt() {
        return categorisedAt;
    }

    public void setCategorisedAt(Instant categorisedAt) {
        this.categorisedAt = categorisedAt;
    }

    public StrategyUsed getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(StrategyUsed strategyUsed) {
        this.strategyUsed = strategyUsed;
    }
}
