CREATE TABLE IF NOT EXISTS translation_requests
(
    id              SERIAL PRIMARY KEY,
    ip_address      VARCHAR(255),
    original_text            TEXT,
    translated_text TEXT
);