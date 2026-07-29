package com.triage.controller;

import com.triage.model.Team;
import com.triage.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamRepository teamRepository;
    
    public TeamController(TeamRepository teamRepository) { 
        this.teamRepository = teamRepository; 
    }
    
    @GetMapping
    public ResponseEntity<List<Team>> getTeams() {
        return ResponseEntity.ok(teamRepository.findAll());
    }
    
    @PutMapping("/{id}/category-mapping")
    public ResponseEntity<Team> updateMapping(@PathVariable Long id, @RequestBody Map<String, Object> mapping) {
        Team team = teamRepository.findById(id).orElseThrow();
        team.setCategoryMapping(mapping);
        return ResponseEntity.ok(teamRepository.save(team));
    }
}
