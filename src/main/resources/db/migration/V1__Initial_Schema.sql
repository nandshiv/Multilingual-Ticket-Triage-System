-- This is a Flyway migration file. Flyway executes scripts in alphabetical order (V1, V2, etc.)
-- This creates our tables based on Section 5 of the spec.

-- Enable the uuid extension so Postgres can auto-generate UUIDs for us
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    tier VARCHAR(50) NOT NULL, -- e.g. BRONZE, SILVER, GOLD, PLATINUM
    region VARCHAR(100),
    preferred_language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_mapping JSONB
);

CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    team_id BIGINT REFERENCES teams(id),
    current_load INT DEFAULT 0
);

CREATE TABLE ticket_clusters (
    id BIGSERIAL PRIMARY KEY,
    root_ticket_id UUID, -- We will add the foreign key constraint at the end to avoid a circular dependency error with the tickets table
    representative_text TEXT,
    ticket_count INT DEFAULT 1,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES customers(id),
    channel VARCHAR(50) NOT NULL,
    raw_text TEXT NOT NULL,
    detected_language VARCHAR(50),
    translated_text TEXT,
    category VARCHAR(100),
    routing_confidence FLOAT,
    assigned_team_id BIGINT REFERENCES teams(id),
    assigned_agent_id BIGINT REFERENCES agents(id),
    priority_score INT,
    priority_breakdown JSONB,
    cluster_id BIGINT REFERENCES ticket_clusters(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Now that tickets table exists, we can add the foreign key to ticket_clusters
ALTER TABLE ticket_clusters ADD CONSTRAINT fk_root_ticket FOREIGN KEY (root_ticket_id) REFERENCES tickets(id);

CREATE TABLE routing_log (
    id BIGSERIAL PRIMARY KEY,
    ticket_id UUID REFERENCES tickets(id),
    model_routed_team_id BIGINT REFERENCES teams(id),
    model_confidence FLOAT,
    final_team_id BIGINT REFERENCES teams(id),
    overridden_by_agent_id BIGINT REFERENCES agents(id),
    overridden_at TIMESTAMP
);
