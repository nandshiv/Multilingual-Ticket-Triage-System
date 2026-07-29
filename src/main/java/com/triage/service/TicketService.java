package com.triage.service;
import com.triage.dto.TicketRequest;
import com.triage.model.*;
import com.triage.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final MlServiceClient mlServiceClient;
    private final TicketEmbeddingRepository embeddingRepository;
    private final TicketClusterRepository clusterRepository;
    private final TeamRepository teamRepository;
    private final RoutingLogRepository routingLogRepository;

    public TicketService(TicketRepository ticketRepository, 
                         CustomerRepository customerRepository, 
                         MlServiceClient mlServiceClient,
                         TicketEmbeddingRepository embeddingRepository,
                         TicketClusterRepository clusterRepository,
                         TeamRepository teamRepository,
                         RoutingLogRepository routingLogRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.mlServiceClient = mlServiceClient;
        this.embeddingRepository = embeddingRepository;
        this.clusterRepository = clusterRepository;
        this.teamRepository = teamRepository;
        this.routingLogRepository = routingLogRepository;
    }

    public Ticket processNewTicket(TicketRequest request) {
        Ticket ticket = new Ticket();
        
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            ticket.setCustomer(customer);
        }
        
        ticket.setChannel(request.getChannel());
        ticket.setRawText(request.getText());
        ticket.setStatus("NEW");
        
        // Phase 2: Language Detection and Translation
        MlServiceClient.TranslationResult translation = mlServiceClient.translate(request.getText());
        ticket.setDetectedLanguage(translation.getDetected_language()); 
        ticket.setTranslatedText(translation.getTranslated_text()); 
        
        ticket = ticketRepository.save(ticket);

        // Phase 4: Clustering via PgVector Windowed Search
        List<Double> embedding = mlServiceClient.embed(ticket.getTranslatedText());
        if (embedding != null && !embedding.isEmpty()) {
            String embeddingStr = embedding.toString();
            embeddingRepository.saveEmbedding(ticket.getId(), embeddingStr);
            
            UUID matchId = embeddingRepository.findNearestDuplicateWithinWindow(embeddingStr, 0.85, ticket.getId());
            if (matchId != null) {
                Ticket matchTicket = ticketRepository.findById(matchId).orElse(null);
                if (matchTicket != null) {
                    TicketCluster cluster = matchTicket.getCluster();
                    if (cluster == null) {
                        cluster = new TicketCluster();
                        cluster.setRootTicketId(matchTicket.getId());
                        cluster.setRepresentativeText(matchTicket.getRawText());
                        cluster.setStatus("ACTIVE");
                        cluster.setTicketCount(1);
                        cluster = clusterRepository.save(cluster);
                        
                        matchTicket.setCluster(cluster);
                        ticketRepository.save(matchTicket);
                    }
                    cluster.setTicketCount(cluster.getTicketCount() + 1);
                    clusterRepository.save(cluster);
                    
                    ticket.setCluster(cluster);
                    ticket.setStatus("MERGED");
                    ticket = ticketRepository.save(ticket);
                }
            }
        }

        // Phase 5: Intent Classification & Routing (if not merged)
        if (!"MERGED".equals(ticket.getStatus())) {
            MlServiceClient.ClassificationResult classification = mlServiceClient.classify(ticket.getTranslatedText());
            ticket.setCategory(classification.getCategory());
            ticket.setRoutingConfidence(classification.getConfidence());
            
            Team assignedTeam = findTeamForCategory(classification.getCategory());
            if (assignedTeam != null) {
                ticket.setAssignedTeam(assignedTeam);
            }
            
            RoutingLog log = new RoutingLog();
            log.setTicketId(ticket.getId());
            log.setModelRoutedTeam(assignedTeam);
            log.setFinalTeam(assignedTeam);
            log.setModelConfidence(classification.getConfidence());
            routingLogRepository.save(log);
            
            ticket = ticketRepository.save(ticket);
        }

        // Phase 6 - Priority Scoring
        calculatePriority(ticket);
        ticket = ticketRepository.save(ticket);

        return ticket;
    }

    private void calculatePriority(Ticket ticket) {
        int score = 0;
        Map<String, Object> breakdown = new java.util.HashMap<>();
        
        if (ticket.getCustomer() != null && ticket.getCustomer().getTier() != null) {
            String tier = ticket.getCustomer().getTier().toUpperCase();
            int tierScore = switch (tier) {
                case "PLATINUM" -> 20;
                case "GOLD" -> 15;
                case "SILVER" -> 5;
                default -> 0;
            };
            score += tierScore;
            breakdown.put("tier", Map.of("value", tier, "points", tierScore));
        }

        String text = ticket.getTranslatedText() != null ? ticket.getTranslatedText().toLowerCase() : "";
        int urgencyScore = 0;
        if (text.contains("stranded") || text.contains("blocked") || text.contains("urgent") || text.contains("outage")) {
            urgencyScore = 30;
        } else if (text.contains("asap") || text.contains("issue") || text.contains("error")) {
            urgencyScore = 10;
        }
        score += urgencyScore;
        breakdown.put("urgency", Map.of("points", urgencyScore));

        int clusterScore = ticket.getCluster() != null ? Math.min(ticket.getCluster().getTicketCount(), 30) : 0;
        score += clusterScore;
        breakdown.put("cluster_size", Map.of("points", clusterScore));

        ticket.setPriorityScore(score);
        ticket.setPriorityBreakdown(breakdown);
    }

    private Team findTeamForCategory(String category) {
        List<Team> teams = teamRepository.findAll();
        for (Team t : teams) {
            Map<String, Object> mapping = t.getCategoryMapping();
            if (mapping != null && mapping.containsKey("categories")) {
                Object cats = mapping.get("categories");
                if (cats instanceof List && ((List<?>) cats).contains(category)) {
                    return t;
                }
            }
        }
        return null;
    }

    public List<Ticket> getFilteredTickets(Long teamId, String status, String language, Boolean clustered) {
        return ticketRepository.findAll(TicketSpecification.filterBy(teamId, status, language, clustered));
    }

    public Ticket updateTicketStatus(UUID id, String newStatus) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
        String currentStatus = ticket.getStatus();
        
        // Enforce state transitions
        if ("MERGED".equals(currentStatus) || "RESOLVED".equals(currentStatus)) {
            throw new IllegalStateException("Cannot change status of a MERGED or RESOLVED ticket");
        }
        
        ticket.setStatus(newStatus.toUpperCase());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket reassignTicket(UUID ticketId, Long teamId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Team newTeam = teamRepository.findById(teamId).orElseThrow();
        Agent newAgent = agentId != null ? agentRepository.findById(agentId).orElse(null) : null;
        
        // Create override log
        RoutingLog log = new RoutingLog();
        log.setTicketId(ticket.getId());
        log.setModelRoutedTeam(ticket.getAssignedTeam());
        log.setFinalTeam(newTeam);
        log.setOverriddenByAgent(newAgent);
        log.setOverriddenAt(LocalDateTime.now());
        routingLogRepository.save(log);
        
        ticket.setAssignedTeam(newTeam);
        ticket.setAssignedAgent(newAgent);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicket(UUID id) {
        return ticketRepository.findById(id);
    }
}
