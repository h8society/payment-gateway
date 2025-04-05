CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE payment_transactions
    ADD COLUMN transaction_uuid VARCHAR(100);

UPDATE payment_transactions
SET transaction_uuid = gen_random_uuid();

ALTER TABLE payment_transactions
    ALTER COLUMN transaction_uuid SET NOT NULL;

ALTER TABLE payment_transactions
    ADD CONSTRAINT uq_transaction_id UNIQUE (transaction_id);

ALTER TABLE payment_transactions
DROP CONSTRAINT payment_transactions_pkey;

ALTER TABLE payment_transactions
DROP COLUMN transaction_id;

ALTER TABLE payment_transactions
    RENAME COLUMN transaction_uuid TO transaction_id;

ALTER TABLE payment_transactions
    ADD PRIMARY KEY (transaction_id);
