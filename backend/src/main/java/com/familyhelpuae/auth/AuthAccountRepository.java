package com.familyhelpuae.auth;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.Optional;

public interface AuthAccountRepository extends Neo4jRepository<AuthAccount, String> {
    Optional<AuthAccount> findByEmail(String email);
}
