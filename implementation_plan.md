# Implementation Plan: FamilyHelpUAE

## Phase 1: Project Initialization
- [ ] Initialize Spring Boot backend project (Spring Web, Spring Data Neo4j, Spring Security, Validation, Lombok).
- [ ] Initialize React + Vite frontend project.
- [ ] Set up basic application properties for Neo4j and JWT.

## Phase 2: Backend Data Model & Repositories
- [ ] Define Neo4j Node Entities (`AuthAccount`, `Family`, `Task`, `Category`, `Region`, `SupportInteraction`, `Feedback`).
- [ ] Define Relationships (`OWNS_PROFILE`, `POSTED`, `ACCEPTED`, `RESULTED_IN`, `WROTE`, `REVIEWS`, `HELPED`, etc.).
- [ ] Create Spring Data Neo4j Repositories with custom Cypher queries.

## Phase 3: Security & Authentication
- [ ] Implement BCrypt password hashing.
- [ ] Implement JWT token generation, parsing, and validation.
- [ ] Create `JwtFilter` and `SecurityConfig`.
- [ ] Implement `AuthController` (register, login, me endpoints).

## Phase 4: Core Business Logic (Services & Controllers)
- [ ] **Family Management**: Profile viewing/updating, trust score calculation and caching.
- [ ] **Task Management**: Create tasks, view task feed (with filters), task details.
- [ ] **Concurrency & Interaction**: Implement atomic task acceptance (with optimistic locking & status guard), task completion, task cancellation.
- [ ] **Feedback System**: Submit feedback, enforce duplicate prevention, trigger trust score recalculation.

## Phase 5: Graph Innovation Features
- [ ] Implement Smart Task Recommendation (scoring based on trust, category, region, neighbors).
- [ ] Implement Common Neighbor Discovery & Degree of Separation.
- [ ] Implement Betweenness Centrality (Community Connector score) and PageRank (Network Trust) in `CentralityServiceImpl`.
- [ ] Implement Network Graph visualization endpoint.

## Phase 6: Documentation & Statistics
- [ ] Configure Swagger/OpenAPI (`OpenApiConfig`).
- [ ] Implement `StatisticsController` (community dashboard).

## Phase 7: Frontend Development
- [ ] Set up routing and Axios interceptors for JWT.
- [ ] Build Login & Registration pages.
- [ ] Build Dashboard and Task Feed.
- [ ] Build Task Creation & Task Details views.
- [ ] Build Family Profile & Network Visualization views.
- [ ] Connect frontend to backend APIs.

## Phase 8: Testing
- [ ] Write integration/concurrency test (e.g., using a Java `ExecutorService` script) for the atomic accept guarantee.
- [ ] Manual functional testing of all endpoints.
