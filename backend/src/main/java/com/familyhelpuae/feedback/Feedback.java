package com.familyhelpuae.feedback;

import com.familyhelpuae.family.Family;
import com.familyhelpuae.interaction.SupportInteraction;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Data
@Node("Feedback")
public class Feedback {
    @Id
    private String feedbackId;
    private int rating;
    private String comment;
    private int reliabilityRating;
    private int communicationRating;
    private LocalDateTime createdAt;
    
    @Relationship(type = "REVIEWS", direction = Relationship.Direction.OUTGOING)
    private Family reviewedFamily;
    
    @Relationship(type = "FOR_INTERACTION", direction = Relationship.Direction.OUTGOING)
    private SupportInteraction interaction;
}
