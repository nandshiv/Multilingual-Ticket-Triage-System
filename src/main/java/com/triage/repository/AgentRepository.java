package com.triage.repository;
import com.triage.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AgentRepository extends JpaRepository<Agent, Long> {}
