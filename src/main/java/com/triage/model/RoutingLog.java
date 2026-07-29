package com.triage.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "routing_log")
public class RoutingLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ticket_id")
    private UUID ticketId;
    @ManyToOne
    @JoinColumn(name = "model_routed_team_id")
    private Team modelRoutedTeam;
    @Column(name = "model_confidence")
    private Double modelConfidence;
    @ManyToOne
    @JoinColumn(name = "final_team_id")
    private Team finalTeam;
    @ManyToOne
    @JoinColumn(name = "overridden_by_agent_id")
    private Agent overriddenByAgent;
    @Column(name = "overridden_at")
    private LocalDateTime overriddenAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
    public Team getModelRoutedTeam() { return modelRoutedTeam; }
    public void setModelRoutedTeam(Team modelRoutedTeam) { this.modelRoutedTeam = modelRoutedTeam; }
    public Double getModelConfidence() { return modelConfidence; }
    public void setModelConfidence(Double modelConfidence) { this.modelConfidence = modelConfidence; }
    public Team getFinalTeam() { return finalTeam; }
    public void setFinalTeam(Team finalTeam) { this.finalTeam = finalTeam; }
    public Agent getOverriddenByAgent() { return overriddenByAgent; }
    public void setOverriddenByAgent(Agent overriddenByAgent) { this.overriddenByAgent = overriddenByAgent; }
    public LocalDateTime getOverriddenAt() { return overriddenAt; }
    public void setOverriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; }
}
