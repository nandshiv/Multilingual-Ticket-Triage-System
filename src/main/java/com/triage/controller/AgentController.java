package com.triage.controller;

import com.triage.model.Ticket;
import com.triage.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final TicketRepository ticketRepository;
    
    public AgentController(TicketRepository ticketRepository) { 
        this.ticketRepository = ticketRepository; 
    }
    
    @GetMapping("/{id}/queue")
    public ResponseEntity<List<Ticket>> getAgentQueue(@PathVariable Long id) {
        List<Ticket> tickets = ticketRepository.findAll((root, query, cb) -> cb.equal(root.get("assignedAgent").get("id"), id));
        tickets.sort((a, b) -> Integer.compare(
            b.getPriorityScore() != null ? b.getPriorityScore() : 0, 
            a.getPriorityScore() != null ? a.getPriorityScore() : 0
        ));
        return ResponseEntity.ok(tickets);
    }
}
