package com.triage.model;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private String channel;
    @Column(name = "raw_text")
    private String rawText;
    @Column(name = "detected_language")
    private String detectedLanguage;
    @Column(name = "translated_text")
    private String translatedText;
    private String category;
    @Column(name = "routing_confidence")
    private Double routingConfidence;
    @ManyToOne
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;
    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private Agent assignedAgent;
    @Column(name = "priority_score")
    private Integer priorityScore;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "priority_breakdown", columnDefinition = "jsonb")
    private Map<String, Object> priorityBreakdown;
    @ManyToOne
    @JoinColumn(name = "cluster_id")
    private TicketCluster cluster;
    private String status;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getRoutingConfidence() { return routingConfidence; }
    public void setRoutingConfidence(Double routingConfidence) { this.routingConfidence = routingConfidence; }
    public Team getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(Team assignedTeam) { this.assignedTeam = assignedTeam; }
    public Agent getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(Agent assignedAgent) { this.assignedAgent = assignedAgent; }
    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
    public Map<String, Object> getPriorityBreakdown() { return priorityBreakdown; }
    public void setPriorityBreakdown(Map<String, Object> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; }
    public TicketCluster getCluster() { return cluster; }
    public void setCluster(TicketCluster cluster) { this.cluster = cluster; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
