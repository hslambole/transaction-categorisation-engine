CREATE TABLE category_rules (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword       VARCHAR(128)  NOT NULL,
    category      VARCHAR(128)  NOT NULL,
    sub_category  VARCHAR(128)  NOT NULL,
    priority      INT           NOT NULL DEFAULT 0,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_category_rules_keyword (keyword)
);