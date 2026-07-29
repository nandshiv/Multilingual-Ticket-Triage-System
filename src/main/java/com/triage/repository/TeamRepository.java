package com.triage.repository;
import com.triage.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TeamRepository extends JpaRepository<Team, Long> {}
