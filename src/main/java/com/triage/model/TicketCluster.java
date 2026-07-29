package com.triage.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_clusters")
public class TicketCluster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "root_ticket_id")
    private UUID rootTicketId;
    @Column(name = "representative_text")
    private String representativeText;
    @Column(name = "ticket_count")
    private Integer ticketCount = 1;
    private String status;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getRootTicketId() { return rootTicketId; }
    public void setRootTicketId(UUID rootTicketId) { this.rootTicketId = rootTicketId; }
    public String getRepresentativeText() { return representativeText; }
    public void setRepresentativeText(String representativeText) { this.representativeText = representativeText; }
    public Integer getTicketCount() { return ticketCount; }
    public void setTicketCount(Integer ticketCount) { this.ticketCount = ticketCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
