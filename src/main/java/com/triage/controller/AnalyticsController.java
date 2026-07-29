package com.triage.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final JdbcTemplate jdbcTemplate;

    public AnalyticsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/volume")
    public List<Map<String, Object>> getVolumeByCategory() {
        return jdbcTemplate.queryForList(
            "SELECT category, COUNT(*) as count FROM tickets GROUP BY category"
        );
    }

    @GetMapping("/routing-accuracy")
    public Map<String, Object> getRoutingAccuracy() {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routing_log", Integer.class);
        if (total == null || total == 0) return Map.of("accuracy", 100.0);
        
        Integer overrides = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routing_log WHERE overridden_by_agent_id IS NOT NULL", Integer.class);
        int overridesCount = overrides != null ? overrides : 0;
        
        double accuracy = ((double) (total - overridesCount) / total) * 100;
        return Map.of("total", total, "overrides", overridesCount, "accuracy", accuracy);
    }

    @GetMapping("/language-distribution")
    public List<Map<String, Object>> getLanguageDistribution() {
        return jdbcTemplate.queryForList(
            "SELECT detected_language, COUNT(*) as count FROM tickets GROUP BY detected_language"
        );
    }
}
