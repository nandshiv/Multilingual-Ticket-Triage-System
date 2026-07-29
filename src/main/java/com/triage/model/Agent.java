package com.triage.model;
import jakarta.persistence.*;

@Entity
@Table(name = "agents")
public class Agent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @Column(name = "current_load")
    private Integer currentLoad = 0;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Integer getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(Integer currentLoad) { this.currentLoad = currentLoad; }
}
