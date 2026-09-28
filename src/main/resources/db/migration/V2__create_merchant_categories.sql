CREATE TABLE merchant_categories (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_name      VARCHAR(255)   NOT NULL,
    category           VARCHAR(128)   NOT NULL,
    sub_category       VARCHAR(128)   NOT NULL,
    confidence_score   DECIMAL(4, 3)  NOT NULL,
    categorised_at     TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    strategy_used      VARCHAR(32)    NOT NULL,
    UNIQUE KEY uk_merchant_categories_merchant (merchant_name)
);