package com.triage.controller;

import com.triage.model.TicketCluster;
import com.triage.model.Ticket;
import com.triage.repository.TicketClusterRepository;
import com.triage.repository.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/clusters")
public class ClusterController {
    
    private final TicketClusterRepository clusterRepository;
    private final TicketRepository ticketRepository;
    
    public ClusterController(TicketClusterRepository clusterRepository, TicketRepository ticketRepository) {
        this.clusterRepository = clusterRepository;
        this.ticketRepository = ticketRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<TicketCluster>> getClusters() {
        return ResponseEntity.ok(clusterRepository.findAll(Sort.by(Sort.Direction.DESC, "ticketCount")));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getClusterDetail(@PathVariable Long id) {
        TicketCluster cluster = clusterRepository.findById(id).orElse(null);
        if (cluster == null) return ResponseEntity.notFound().build();
        
        List<Ticket> tickets = ticketRepository.findAll((root, query, cb) -> cb.equal(root.get("cluster"), cluster));
        return ResponseEntity.ok(java.util.Map.of("cluster", cluster, "tickets", tickets));
    }
    
    @PostMapping("/{id}/resolve")
    @Transactional
    public ResponseEntity<?> resolveCluster(@PathVariable Long id) {
        TicketCluster cluster = clusterRepository.findById(id).orElse(null);
        if (cluster == null) return ResponseEntity.notFound().build();
        
        cluster.setStatus("RESOLVED");
        clusterRepository.save(cluster);
        
        List<Ticket> tickets = ticketRepository.findAll((root, query, cb) -> cb.equal(root.get("cluster"), cluster));
        for (Ticket t : tickets) {
            t.setStatus("RESOLVED");
            ticketRepository.save(t);
        }
        
        return ResponseEntity.ok(java.util.Map.of("message", "Cluster resolved", "ticketsUpdated", tickets.size()));
    }
}
