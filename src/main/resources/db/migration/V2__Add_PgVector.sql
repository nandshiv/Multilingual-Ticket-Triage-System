-- Add pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create table to store embeddings
CREATE TABLE ticket_embeddings (
    ticket_id UUID PRIMARY KEY REFERENCES tickets(id),
    embedding vector(384)
);
