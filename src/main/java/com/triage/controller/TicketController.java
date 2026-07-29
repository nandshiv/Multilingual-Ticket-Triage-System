package com.triage.controller;
import com.triage.dto.TicketRequest;
import com.triage.model.Ticket;
import com.triage.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketRequest request) {
        Ticket ticket = ticketService.processNewTicket(request);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getTickets(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean clustered) {
        return ResponseEntity.ok(ticketService.getFilteredTickets(teamId, status, language, clustered));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable UUID id) {
        return ticketService.getTicket(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/team")
    public ResponseEntity<Ticket> reassignTeam(
            @PathVariable UUID id, 
            @RequestParam Long teamId, 
            @RequestParam(required = false) Long agentId) {
        return ResponseEntity.ok(ticketService.reassignTicket(id, teamId, agentId));
    }
}
