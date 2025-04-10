-- Добавление таблицы магазинов
CREATE TABLE shops (
                       shop_id BIGSERIAL PRIMARY KEY,
                       merchant_id BIGINT NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_shop_merchant FOREIGN KEY (merchant_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Добавление полей shop_id и order_number в таблицу транзакций
ALTER TABLE payment_transactions
    ADD COLUMN shop_id BIGINT;

ALTER TABLE payment_transactions
    ADD COLUMN order_number VARCHAR(255) UNIQUE;

-- Внешний ключ на магазин
ALTER TABLE payment_transactions
    ADD CONSTRAINT fk_transaction_shop FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE SET NULL;


INSERT INTO gateway_settings (setting_key, value)
VALUES
    ('ORDER_NUMBER_REQUIRED', 'true');

CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1 INCREMENT BY 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_order_number ON payment_transactions(order_number);