package com.familyhelpuae.feedback;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FeedbackRepository extends Neo4jRepository<Feedback, String> {
}
