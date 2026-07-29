package com.triage.model;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_mapping", columnDefinition = "jsonb")
    private Map<String, Object> categoryMapping;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getCategoryMapping() { return categoryMapping; }
    public void setCategoryMapping(Map<String, Object> categoryMapping) { this.categoryMapping = categoryMapping; }
}
