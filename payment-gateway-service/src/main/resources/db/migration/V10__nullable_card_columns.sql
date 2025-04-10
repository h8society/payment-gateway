ALTER TABLE payment_transactions ALTER COLUMN card_number_enc DROP NOT NULL;
ALTER TABLE payment_transactions ALTER COLUMN card_expiry_enc DROP NOT NULL;
ALTER TABLE payment_transactions ALTER COLUMN card_cvc_enc DROP NOT NULL;