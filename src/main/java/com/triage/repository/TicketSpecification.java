package com.triage.repository;

import com.triage.model.Ticket;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {
    public static Specification<Ticket> filterBy(Long teamId, String status, String language, Boolean clustered) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (teamId != null) {
                predicates.add(cb.equal(root.get("assignedTeam").get("id"), teamId));
            }
            if (status != null && !status.isEmpty()) {
                if ("ALL".equalsIgnoreCase(status)) {
                    // Ignore ALL
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }
            if (language != null && !language.isEmpty()) {
                predicates.add(cb.equal(root.get("detectedLanguage"), language));
            }
            if (clustered != null) {
                if (clustered) {
                    predicates.add(cb.isNotNull(root.get("cluster")));
                } else {
                    predicates.add(cb.isNull(root.get("cluster")));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
