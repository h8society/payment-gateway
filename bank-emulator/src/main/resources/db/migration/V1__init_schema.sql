CREATE TABLE IF NOT EXISTS card_response_mapping (
    mapping_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_number VARCHAR(16) NOT NULL UNIQUE,
    response_code VARCHAR(10) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
