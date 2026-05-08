package com.familyhelpuae.auth;

import com.familyhelpuae.family.Family;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Data
@Node("AuthAccount")
public class AuthAccount {
    @Id
    private String accountId;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    @Relationship(type = "OWNS_PROFILE", direction = Relationship.Direction.OUTGOING)
    private Family family;
}
