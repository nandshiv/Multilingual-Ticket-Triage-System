package com.triage.repository;

import com.triage.model.Ticket;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface TicketEmbeddingRepository extends org.springframework.data.repository.Repository<Ticket, UUID> {
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ticket_embeddings (ticket_id, embedding) VALUES (:ticketId, cast(:embedding as vector))", nativeQuery = true)
    void saveEmbedding(@Param("ticketId") UUID ticketId, @Param("embedding") String embedding);

    @Query(value = "SELECT e.ticket_id " +
                   "FROM ticket_embeddings e " +
                   "JOIN tickets t ON e.ticket_id = t.id " +
                   "WHERE t.created_at >= NOW() - INTERVAL '6 hours' " +
                   "AND e.ticket_id != :excludeTicketId " +
                   "AND 1 - (e.embedding <=> cast(:queryEmbedding as vector)) >= :threshold " +
                   "ORDER BY e.embedding <=> cast(:queryEmbedding as vector) LIMIT 1", nativeQuery = true)
    UUID findNearestDuplicateWithinWindow(@Param("queryEmbedding") String queryEmbedding, 
                                          @Param("threshold") double threshold,
                                          @Param("excludeTicketId") UUID excludeTicketId);
}
