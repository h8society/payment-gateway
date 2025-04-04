-- V2__update_schema.sql
ALTER TABLE payment_transactions
    ADD COLUMN IF NOT EXISTS bin_brand VARCHAR(50),
    ADD COLUMN IF NOT EXISTS bin_bank_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bin_country VARCHAR(50);
