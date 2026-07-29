# Multilingual Support Ticket Triage System

An intelligent backend system and Agent Dashboard that automatically deduplicates, classifies, and prioritizes customer support tickets across multiple languages.

## Getting Started

1. **Start the Infrastructure** (PostgreSQL with pgvector & Python ML Microservice)
```bash
docker-compose up -d
```

2. **Start the Spring Boot Backend**
Run `TicketTriageApplication.java` from your IDE, or run:
```bash
./mvnw spring-boot:run
```

3. **View the Agent Dashboard**
Open your browser to `http://localhost:8080/index.html` (Spring Boot automatically serves the static HTML/JS/CSS files).

---

## Key Architectural Decisions (Interview Talking Points)

### 1. Windowed & Indexed Similarity Search for Deduplication
When detecting duplicate tickets (clustering), computing cosine similarity against the entire historical database is `O(n^2)` and won't scale in production. 
Instead, we implemented a **Rolling-Window Search**:
- We use the `pgvector` Postgres extension to handle embeddings natively alongside transactional data.
- The system only compares incoming embeddings against tickets created in the last 6 hours (`t.created_at >= NOW() - INTERVAL '6 hours'`).
- This limits the search space to a manageable size, ensuring horizontal scaling characteristics regardless of how large the database grows over years.

### 2. Explainable Priority Scoring (Not a Black Box)
While we use ML for embedding generation and classification, we deliberately chose **not** to use a black-box model for priority scoring.
- Priority is determined via a transparent weighted scoring engine (e.g., Customer Tier = +20, Urgency Keywords = +30, Cluster Size = +1 per affected user).
- This decision ensures that customer support agents can inspect the exact point breakdown in the UI, understanding *why* a ticket is ranked high and building trust in the system.

### 3. Microservice Boundary
We separated the ML components (Python/FastAPI) from the transactional core (Java/Spring Boot). This is a standard industry pattern, allowing us to use battle-tested ML libraries (like `sentence-transformers`) without polluting the JVM, while keeping business logic and state management strictly in Spring Boot.
