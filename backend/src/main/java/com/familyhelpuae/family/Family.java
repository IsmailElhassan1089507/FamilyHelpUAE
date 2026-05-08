package com.familyhelpuae.family;

import com.familyhelpuae.region.Region;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Data
@Node("Family")
public class Family {
    @Id
    private String familyId;
    private String name;
    private String verificationStatus = "UNVERIFIED";
    private int trustScore = 50;
    private int completedHelpCount = 0;
    private int cancelledTaskCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    
    // Cached scores
    private double betweennessScore = 0.0;
    private double networkTrustScore = 50.0;

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private Region region;
}
