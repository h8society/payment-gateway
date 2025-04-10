ALTER TABLE payment_transactions
    ADD COLUMN original_transaction_id VARCHAR(100) REFERENCES payment_transactions(transaction_id) ON DELETE SET NULL;