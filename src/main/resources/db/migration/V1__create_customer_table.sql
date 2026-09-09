CREATE TABLE customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    siret VARCHAR(14) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    address_line VARCHAR(255),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
