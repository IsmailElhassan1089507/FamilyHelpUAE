package com.familyhelpuae.interaction;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Data
@Node("SupportInteraction")
public class SupportInteraction {
    @Id
    private String interactionId;
    private String status; // IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
