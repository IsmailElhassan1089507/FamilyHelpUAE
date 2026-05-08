package com.familyhelpuae.task;

import com.familyhelpuae.category.Category;
import com.familyhelpuae.region.Region;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Data
@Node("Task")
public class Task {
    @Id
    private String taskId;
    private String title;
    private String description;
    
    private String type; // OFFER or REQUEST
    private String status; // OPEN, IN_PROGRESS, COMPLETED, CANCELLED, EXPIRED
    private String priority;
    
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    
    @Version
    private Long version;
    
    @Relationship(type = "IN_CATEGORY", direction = Relationship.Direction.OUTGOING)
    private Category category;
    
    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private Region region;
}
