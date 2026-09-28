CREATE TABLE transactions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_name     VARCHAR(255)    NOT NULL,
    amount            DECIMAL(19, 4)  NOT NULL,
    currency          CHAR(3)         NOT NULL,
    transaction_date  DATE            NOT NULL,
    source            VARCHAR(32)     NOT NULL,
    created_at        TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);