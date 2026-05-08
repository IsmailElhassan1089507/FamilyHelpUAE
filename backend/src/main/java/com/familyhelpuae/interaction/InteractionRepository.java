package com.familyhelpuae.interaction;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface InteractionRepository extends Neo4jRepository<SupportInteraction, String> {
}
