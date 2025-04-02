-- 1) Таблица users
CREATE TABLE users (
                       user_id         BIGSERIAL PRIMARY KEY,
                       username        VARCHAR(50)  NOT NULL UNIQUE,
                       email           VARCHAR(100) NOT NULL UNIQUE,
                       hashed_password VARCHAR(255) NOT NULL,
                       created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2) Таблица roles
CREATE TABLE roles (
                       role_id   BIGSERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT
);

INSERT INTO roles (role_id, role_name, description)
VALUES
    (1, 'ADMIN', 'Role for admins'),
    (2, 'MERCHANT', 'Role for merchants');

-- 3) Таблица user_roles
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(user_id),
                            CONSTRAINT fk_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(role_id)
);

-- 4) Таблица transaction_statuses
CREATE TABLE transaction_statuses (
                                      status_id   BIGSERIAL PRIMARY KEY,
                                      status_code VARCHAR(20) NOT NULL UNIQUE,
                                      description TEXT
);

INSERT INTO transaction_statuses(status_code, description)
VALUES
    ('created', 'Создана'),
    ('declined', 'Отклонена'),
    ('paid', 'Оплачена'),
    ('canceled', 'Отменена'),
    ('refund', 'Возврат');

-- 5) Таблица gateway_settings
CREATE TABLE gateway_settings (
                                  setting_key VARCHAR(100) NOT NULL PRIMARY KEY,
                                  value       TEXT NOT NULL
);

-- 6) Таблица merchant_keys
CREATE TABLE merchant_keys (
                               key_id     BIGSERIAL PRIMARY KEY,
                               user_id    BIGINT NOT NULL,
                               api_key    VARCHAR(100) NOT NULL UNIQUE,
                               description TEXT,
                               created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               valid_until TIMESTAMP,
                               CONSTRAINT fk_user_merchant_keys
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(user_id)
);

-- 7) Таблица payment_transactions
CREATE TABLE payment_transactions (
                                      transaction_id   BIGSERIAL PRIMARY KEY,
                                      user_id          BIGINT NOT NULL,
                                      key_id           BIGINT NOT NULL,
                                      card_number_enc  VARCHAR(255) NOT NULL,
                                      card_expiry_enc  VARCHAR(255) NOT NULL,
                                      card_cvc_enc     VARCHAR(255) NOT NULL,
                                      amount           DECIMAL(19,2) NOT NULL,
                                      response_code    VARCHAR(255)  NOT NULL,
                                      transaction_date TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      status_id        BIGINT NOT NULL,
                                      CONSTRAINT fk_user_payment_transactions
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(user_id),
                                      CONSTRAINT fk_merchant_key_payment_transactions
                                          FOREIGN KEY (key_id)
                                              REFERENCES merchant_keys(key_id),
                                      CONSTRAINT fk_status_payment_transactions
                                          FOREIGN KEY (status_id)
                                              REFERENCES transaction_statuses(status_id)
);
