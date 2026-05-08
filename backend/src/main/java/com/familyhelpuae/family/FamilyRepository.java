package com.familyhelpuae.family;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FamilyRepository extends Neo4jRepository<Family, String> {
}
