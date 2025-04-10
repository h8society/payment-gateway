-- Добавление тестового мерчанта
INSERT INTO users (user_id, username, email, hashed_password, created_at, active) VALUES
    (2001, 'testmerchant', 'merchant@example.com', '$2b$12$tDUMiCqfXi3TRx0OeiLdO.BHTi.y3mt8JB2ObpVpPAjscUQt6z6WG', NOW(), true);

-- Назначение роли MERCHANT
INSERT INTO user_roles (user_id, role_id) VALUES (2001, 2);

-- Добавление двух магазинов
INSERT INTO shops (shop_id, name, description, merchant_id) VALUES
                                                                (3001, 'Магазин А', 'Описание магазина A', 2001),
                                                                (3002, 'Магазин Б', 'Описание магазина B', 2001);

-- Добавление тестового ключа мерчанта
INSERT INTO merchant_keys (user_id, api_key, description)
VALUES (2001, 'TEST-API-KEY-2001', 'Тестовый ключ для мерчанта 2001');

DO $$
DECLARE
i               INT;
    uid             CONSTANT BIGINT := 2001;
    sid             BIGINT;
    shop_id_val     BIGINT;
    key_id_val      BIGINT;
    amount          NUMERIC;
    txn_date        DATE;
    card_number_enc VARCHAR(255);
    card_expiry_enc VARCHAR(255);
    card_cvc_enc    VARCHAR(255);
    response_code   VARCHAR(255);
    order_number_val VARCHAR(255);
    v_status_code   TEXT;
BEGIN
FOR i IN 1..100 LOOP
        shop_id_val := (
            SELECT s.shop_id
            FROM shops s
            WHERE s.merchant_id = uid
            ORDER BY random()
            LIMIT 1
        );

        key_id_val := (
            SELECT mk.key_id
            FROM merchant_keys mk
            WHERE mk.user_id = uid
            ORDER BY random()
            LIMIT 1
        );

        order_number_val := 'ORDER-' || i;

        txn_date := DATE '2025-01-01' + (random() * (DATE '2025-04-07' - DATE '2025-01-01'))::INT;
        amount    := round((random() * 900 + 100)::numeric, 2);

        v_status_code := (ARRAY['created', 'declined', 'paid', 'canceled', 'refund'])[floor(random() * 5 + 1)];
        sid := (
            SELECT ts.status_id
            FROM transaction_statuses ts
            WHERE ts.status_code = v_status_code
        );

        card_number_enc := 'enc-4111111111111111';
        card_expiry_enc := 'enc-12/25';
        card_cvc_enc    := 'enc-123';
        response_code   := '00';

INSERT INTO payment_transactions (
    transaction_id,
    user_id,
    key_id,
    card_number_enc,
    card_expiry_enc,
    card_cvc_enc,
    amount,
    response_code,
    transaction_date,
    status_id,
    shop_id,
    order_number
) VALUES (
             i,
             uid,
             key_id_val,
             card_number_enc,
             card_expiry_enc,
             card_cvc_enc,
             amount,
             response_code,
             txn_date,
             sid,
             shop_id_val,
             order_number_val
         );
END LOOP;
END $$;
