# Comprehensive Requirements Document

**FamilyHelpUAE™ — Secure RESTful Graph-Based Community Family Support Platform**

## 1. Project Purpose

FamilyHelpUAE™ is a distributed, secure, RESTful API-based web application that allows families in local UAE communities to offer and request practical support services such as tutoring, childcare, transportation, elder care, and household assistance.

The system must satisfy the assignment requirements by implementing a secure Spring Boot REST API, frontend application, authentication and authorization, trust/reputation mechanisms, scalability and concurrency handling, testing, Swagger documentation, and a self-evaluation report. The project will also include a graph-powered innovation layer using Neo4j to support trust-based recommendations, common-neighbor discovery, degree of separation, and community network visualization.

## 2. System Scope

### 2.1 In Scope

The application shall include:

- Family registration and login
- JWT-based authentication
- Family profile management
- Help offer creation
- Help request creation
- Task listing and filtering
- Task acceptance
- Task rejection / cancellation
- Task completion
- Feedback submission
- Trust score calculation
- Reputation display
- Neo4j graph-based recommendation engine
- Degree-of-separation calculation
- Common-neighbor discovery
- Community network visualization
- Public community statistics dashboard
- Swagger API documentation
- Test cases and results
- Concurrent user simulation

## 3. System Architecture Requirements

### 3.1 Required Technology Stack

The system shall use the following architecture:

| Layer | Technology | Purpose |
| --- | --- | --- |
| Frontend | JavaScript / React (Vite) | User interface |
| Backend | Java Spring Boot | REST API and business logic |
| Database | Neo4j AuraDB Free | Graph persistence and analytics |
| Data Access | Spring Data Neo4j + custom Cypher | Neo4j integration |
| Security | Spring Security, JWT, BCrypt | Authentication and authorization |
| Documentation | Swagger / Springdoc OpenAPI | API documentation |
| Testing | Postman, JMeter / k6 / ExecutorService | Functional and concurrency testing |
| Deployment | Local development only — frontend on http://localhost:5173, backend on http://localhost:8080, both running on the same host machine. No production deployment is in scope for this assignment. | Runtime environment |

### 3.2 Communication Architecture

The system shall follow this architecture:

`Multiple browser windows / tabs (same machine)`

`↓`

`React frontend (http://localhost:5173)`

`↓`

`Spring Boot REST API (http://localhost:8080)`

`↓`

`Neo4j AuraDB Free (cloud, accessed only by backend)`

The frontend shall never connect directly to Neo4j.

Demo setup: multiple browser windows or tabs on the same machine, each logged in as a different family (Family A, B, C). This simulates concurrent multi-family interaction without requiring network deployment.

## 4. Functional Requirements

### FR1 — Family Registration

#### Requirement

The system shall allow a new family to register.

#### Required input

- Family name
- Email
- Password
- Region

#### Validation rules

- Family name must not be empty.
- Email must be valid and unique.
- Password must meet minimum length requirements.
- Region must be selected from supported regions.

#### Processing rules

- The password must be hashed using BCrypt.
- An AuthAccount node must be created.
- A Family node must be created.
- AuthAccount must be connected to Family using OWNS_PROFILE.
- The default trust score must be neutral, for example 50.

#### Expected Neo4j structure

`(:AuthAccount)-[:OWNS_PROFILE]->(:Family)`

#### Acceptance criteria

- A valid registration creates a new family account.
- Duplicate email registration is rejected.
- Password hash is stored, not the plain password.
- API responses never return passwordHash.

### FR2 — Family Login

#### Requirement

The system shall allow registered families to log in using email and password.

#### Required input

- Email
- Password

#### Processing rules

- System finds AuthAccount by email.
- System compares password using BCrypt.
- If valid, system returns a JWT.
- If invalid, system returns an authentication error.

#### Acceptance criteria

- Valid credentials return a JWT.
- Invalid credentials are rejected.
- JWT is required for protected endpoints.

### FR3 — Current Authenticated User

#### Requirement

The system shall provide an endpoint to retrieve the currently authenticated user.

#### Endpoint

`GET /api/auth/me`

#### Response should include

- familyId
- familyName
- email
- region
- trustScore

#### Response must not include

- passwordHash
- JWT secret
- Neo4j credentials
- internal Neo4j element IDs

### FR4 — Family Profile Management

#### Requirement

The system shall allow a family to view and update its own profile.

#### Editable fields

- Family name
- Region
- Short description / bio
- Contact preference (optional)

#### Authorization rules

- A family can update only its own profile.
- A family cannot update another family's profile.

#### Acceptance criteria

- Profile update succeeds for the owner.
- Profile update fails for non-owner.
- Public profile data can be viewed by other authenticated families.

### FR5 — Create Help Task

#### Requirement

The system shall allow authenticated families to create help offers or help requests.

#### Task types

- OFFER
- REQUEST

#### Required task fields

- Title
- Description
- Type
- Category
- Region
- Scheduled date/time
- Priority

#### Optional fields

- Estimated duration
- Additional notes

#### Default values

- status = OPEN
- createdAt = current datetime
- version = 0

#### Valid statuses

- OPEN
- IN_PROGRESS
- COMPLETED
- CANCELLED
- EXPIRED

#### Expected Neo4j structure

`(:Family)-[:POSTED]->(:Task)`

`(:Task)-[:IN_CATEGORY]->(:Category)`

`(:Task)-[:LOCATED_IN]->(:Region)`

#### Acceptance criteria

- Only authenticated families can create tasks.
- Created tasks are OPEN by default.
- Task is linked to creator, category, and region.
- Invalid task type is rejected.
- Invalid category is rejected.
- Invalid region is rejected.

### FR6 — View Task Feed

#### Requirement

The system shall allow users to view available help offers and requests.

#### Endpoint

`GET /api/tasks`

#### Supported filters

- status
- type
- category
- region
- priority

#### Example

`GET /api/tasks?status=OPEN&type=REQUEST&category=Tutoring®ion=KhalifaCity`

#### Acceptance criteria

- Default feed shows OPEN tasks.
- Results are sorted by newest first.
- Results use pagination or a fixed limit.
- Users can filter by type, category, region, and status.

### FR7 — View Task Details

#### Requirement

The system shall allow users to view details of a specific task.

#### Endpoint

`GET /api/tasks/{taskId}`

#### Response should include

- taskId
- title
- description
- type
- status
- category
- region
- priority
- createdAt
- scheduledAt
- postedBy
- acceptedBy (if any)

#### Acceptance criteria

- Existing task returns full task details.
- Non-existing task returns 404.
- Sensitive data is not returned.

### FR8 — Accept Task

#### Requirement

The system shall allow an authenticated family to accept an OPEN task posted by another family.

#### Endpoint

`POST /api/tasks/{taskId}/accept`

#### Authorization rules

- Only authenticated families can accept tasks.
- A family cannot accept its own task.
- Only OPEN tasks can be accepted.
- Only one family can accept a task.

#### Processing rules

When a task is accepted:

- Task status changes from OPEN to IN_PROGRESS.
- ACCEPTED relationship is created.
- SupportInteraction node is created.
- Task is linked to SupportInteraction.
- Requester / helper roles are assigned.
- Task.version is incremented.

#### Expected Neo4j structure

`(:Family)-[:ACCEPTED]->(:Task)`

`(:Task)-[:RESULTED_IN]->(:SupportInteraction)`

`(:Family)-[:REQUESTER_IN]->(:SupportInteraction)`

`(:Family)-[:HELPER_IN]->(:SupportInteraction)`

#### Important rule for OFFER vs REQUEST

If the task type is REQUEST: posted family = requester; accepted family = helper.

If the task type is OFFER: posted family = helper; accepted family = requester.

#### Concurrency requirement

Task acceptance shall be implemented as a single conditional Cypher statement inside a Spring @Transactional service method. The statement guards on t.status = 'OPEN', increments t.version, and atomically creates the ACCEPTED relationship and SupportInteraction node in the same transaction. The combination of the status guard and version increment guarantees that exactly one of N concurrent acceptance attempts succeeds.

#### Optimistic locking via version

The atomic accept Cypher query shall increment Task.version as part of the same MATCH ... WHERE t.status = 'OPEN' SET ... clause. The query reads the current version and writes version + 1 only when the status condition still holds. Under concurrent contention, only one writer's SET succeeds at the storage layer; all other transactions either match zero rows (because status flipped to IN_PROGRESS) or fail their conditional update. This provides optimistic-locking semantics as a defence-in-depth pattern alongside the status guard, and the version field gives the service layer a clean primitive for any future endpoint that needs check-and-set behaviour on tasks.

#### Acceptance criteria

- One family can accept an open task.
- A family cannot accept its own task.
- Already accepted tasks cannot be accepted again.
- Under concurrent requests, only one request succeeds.
- All other concurrent requests receive a controlled error response.
- Task.version is strictly monotonically increasing across successful accepts.

### FR9 — Cancel Task

#### Requirement

The system shall allow a task to be cancelled under controlled conditions.

#### Endpoint

`POST /api/tasks/{taskId}/cancel`

#### Authorization rules

- Task creator can cancel an OPEN task.
- Participants may cancel an IN_PROGRESS task if allowed by business rule.

#### Processing rules

- Task status changes to CANCELLED.
- Cancellation timestamp is recorded.
- Cancellation count may affect trust score if the family accepted and then cancelled.

#### Acceptance criteria

- Open task can be cancelled by creator.
- Completed task cannot be cancelled.
- Cancelled task cannot be accepted.
- Cancellation may contribute to trust penalty.

### FR10 — Complete Task

#### Requirement

The system shall allow involved families to complete a task.

#### Endpoint

`POST /api/tasks/{taskId}/complete`

#### Authorization rules

- Only requester or helper can complete the task.
- Only IN_PROGRESS tasks can be completed.
- Unauthenticated users cannot complete tasks.

#### Processing rules

- Task status changes to COMPLETED.
- Task completedAt timestamp is recorded.
- SupportInteraction status changes to COMPLETED.
- SupportInteraction completedAt timestamp is recorded.
- Feedback becomes allowed after completion.

#### Acceptance criteria

- Participant can complete task.
- Non-participant cannot complete task.
- OPEN task cannot be completed directly unless accepted first.
- COMPLETED task cannot be completed again.

### FR11 — Submit Feedback

#### Requirement

The system shall allow families to submit feedback after a completed interaction.

#### Endpoint

`POST /api/interactions/{interactionId}/feedback`

#### Required fields

- revieweeFamilyId
- rating
- comment
- reliabilityRating
- communicationRating

#### Validation rules

- rating must be between 1 and 5.
- reliabilityRating must be between 1 and 5.
- communicationRating must be between 1 and 5.
- comment must not exceed maximum length.

#### Authorization rules

- Only participants in the SupportInteraction can submit feedback.
- Feedback can only be submitted after task completion.
- A reviewer can submit only one feedback per interaction.
- A family cannot review itself.

#### Expected Neo4j structure

`(:Family)-[:WROTE]->(:Feedback)`

`(:Feedback)-[:REVIEWS]->(:Family)`

`(:Feedback)-[:FOR_INTERACTION]->(:SupportInteraction)`

#### Acceptance criteria

- Feedback is saved for completed interaction.
- Feedback is rejected before task completion.
- Duplicate feedback is rejected.
- Self-review is rejected.
- Trust score updates after feedback.

### FR12 — Create HELPED Relationship

#### Requirement

The system shall create a graph analytics relationship after successful support completion.

#### Relationship

`(helper Family)-[:HELPED]->(requester Family)`

#### Relationship properties

- interactionId
- taskId
- category
- rating
- date

#### Processing rules

- HELPED is created only after a completed task.
- HELPED is used for graph analytics and recommendations.
- HELPED must not be created for cancelled tasks.
- HELPED must not be created for incomplete tasks.

#### Acceptance criteria

- Completed interaction creates HELPED edge.
- HELPED edge direction is from helper to requester.
- Relationship includes task, category, rating, and date.
- Neo4j Browser can visualize the completed help graph.

### FR13 — View Feedback for Family

#### Requirement

The system shall allow users to view feedback received by a family.

#### Endpoint

`GET /api/families/{familyId}/feedback`

#### Response should include

- rating
- comment
- reviewer family name
- createdAt
- interaction category

#### Acceptance criteria

- Feedback list is returned for valid family.
- Results are sorted by newest first.
- Private account data is not exposed.

### FR14 — View Family Trust Score

#### Requirement

The system shall calculate and return a family trust score.

#### Endpoint

`GET /api/families/{familyId}/trust-score`

#### Trust score range

0 to 100

#### Score factors

- Average feedback rating
- Number of completed help interactions
- Cancellation penalty
- Reliability rating
- Communication rating

#### Required behavior

- Trust score is recalculated after feedback.
- Trust score is cached on Family node.
- Trust score is visible on family profiles.

#### Acceptance criteria

- Trust score is never below 0.
- Trust score is never above 100.
- New families start with neutral trust score.
- Trust formula is documented in the report and Swagger description.

### FR15 — Public Community Statistics

#### Requirement

The system shall provide a basic public dashboard endpoint.

#### Endpoint

`GET /api/statistics/community`

#### Response should include

- Total families
- Total tasks
- Open tasks
- In-progress tasks
- Completed tasks
- Cancelled tasks
- Average trust score
- Total feedback count
- Total HELPED relationships
- Most common task category

#### Authorization rules

Any authenticated family user can access the community statistics.

## 5. Graph-Powered Innovation Requirements

### GIR1 — Smart Task Recommendation

#### Requirement

The system shall recommend suitable families for a specific task.

#### Endpoint

`GET /api/tasks/{taskId}/recommended-families`

#### Recommendation factors

- Trust score
- Category expertise
- Same region
- Common neighbors
- Completed interaction history
- Cancellation count

#### Scoring model

The system shall calculate a recommendation score using the following weighted formula:

- 30% direct trust score
- 25% category expertise
- 15% same-region score
- 15% common-neighbor score
- 10% network trust (PageRank — GIR7)
- 5% reliability / cancellation factor

#### Response should include

- familyId
- familyName
- region
- trustScore
- categoryExperience
- commonNeighbors
- recommendationScore
- reason

#### Example reason

“Recommended because this family has a high trust score, tutoring experience, and 3 mutual community connections.”

#### Acceptance criteria

- Recommended families are ranked.
- Requester is not recommended for their own task.
- Families with higher trust and category experience rank higher.
- Each recommendation includes an explanation.

### GIR2 — Common Neighbor Discovery

#### Requirement

The system shall suggest family connections using common neighbors in the HELPED graph.

#### Endpoint

`GET /api/families/{familyId}/suggested-connections`

#### Graph logic

The system shall find families connected through shared support relationships.

Example:

- Family A helped Family B.
- Family C helped Family B.
- Family A and Family C share Family B as a common neighbor.

#### Response should include

- candidateFamilyId
- candidateFamilyName
- trustScore
- commonNeighborCount

#### Acceptance criteria

- System returns families with mutual graph connections.
- System does not return the requesting family.
- System limits results to avoid expensive graph traversal.

### GIR3 — Degree of Separation

#### Requirement

The system shall calculate the shortest path between two families using the HELPED graph.

#### Endpoint

`GET /api/families/{familyId}/degree-of-separation/{otherFamilyId}`

#### Graph rule

The system shall use bounded shortest-path traversal.

`Example: [:HELPED*..5]`

#### Response should include

- sourceFamilyId
- targetFamilyId
- degreeOfSeparation
- pathExists

#### Acceptance criteria

- If a path exists, system returns path length.
- If no path exists, system returns pathExists = false.
- Traversal depth is bounded.

### GIR4 — Category Expertise

#### Requirement

The system shall calculate a family's experience in a specific category.

#### Endpoint

`GET /api/families/{familyId}/expertise?category=Tutoring`

#### Score factors

- Number of completed HELPED relationships in category
- Average rating in category
- Recent completion history

#### Acceptance criteria

- System returns category-specific completed task count.
- System returns category-specific average rating.
- Category expertise contributes to recommendations.

### GIR5 — Network Visualization

#### Requirement

The system shall provide graph data for frontend visualization.

#### Endpoint

`GET /api/families/{familyId}/network`

#### Response format

`{`

`"nodes": [`

`{`

`"id": "family-id",`

`"name": "Al Noor Family",`

`"trustScore": 88,`

`"region": "Khalifa City"`

`}`

`],`

`"links": [`

`{`

`"source": "family-id-1",`

`"target": "family-id-2",`

`"type": "HELPED",`

`"category": "Tutoring",`

`"rating": 5`

`}`

`]`

`}`

#### Graph depth limit

1 to 2 hops from selected family.

#### Acceptance criteria

- Endpoint returns nodes and links.
- Frontend can render the graph.
- Graph is limited to avoid performance issues.
- HELPED relationships are visible.

### GIR6 — Community Connector Score (Betweenness Centrality)

The system shall compute a betweenness-centrality score over the HELPED graph and expose families that act as “bridges” between sub-communities. Families with high betweenness connect otherwise disconnected groups and are valuable to the community.

#### Endpoint

`GET /api/families/community-connectors?limit=10`

#### Implementation approach

Computed in application code using a sampled Brandes algorithm. The backend periodically (e.g., on application startup and after every N completed interactions) loads the HELPED subgraph via a bounded Cypher query, runs Brandes' single-source shortest-paths-based betweenness computation in Java over the in-memory graph, normalises the result to 0–100, and writes betweennessScore back onto each Family node. Sample size is bounded to keep computation tractable. The graph is small enough (community-scale, hundreds to low thousands of nodes) that this is well within reach of a single Spring Boot process. The Neo4j Graph Data Science (GDS) library is not used — AuraDB Free does not include the GDS plugin.

#### Response shall include

- familyId
- familyName
- region
- betweennessScore
- rank
- badgeTier (Gold / Silver / Bronze)

#### Acceptance criteria

- Endpoint returns ranked list.
- Score is bounded and normalised.
- Computation is bounded in graph depth and node count.
- UI surfaces a “Community Connector” badge on profiles ranked in the top 10%.

### GIR7 — Trust Propagation (PageRank over HELPED)

The system shall compute a propagated trust signal using a personalized PageRank-style algorithm where trust flows along HELPED edges weighted by feedback rating. This complements the direct trust score (FR14) by capturing indirect trust (“families trusted by families I trust”).

#### Endpoint

`GET /api/families/{familyId}/network-trust`

#### Implementation approach

Computed in application code using a power-iteration PageRank implementation in Java. The backend loads the rating-weighted HELPED subgraph via Cypher, runs power iteration to convergence (typically 20–50 iterations with damping factor 0.85) or to a maximum iteration cap, normalises results to 0–100, and caches networkTrustScore on each Family node. Recomputation is triggered after batches of completed interactions, not on every read. The Neo4j Graph Data Science (GDS) library is not used — AuraDB Free does not include the GDS plugin.

#### Use case

Recommendation Score formula (GIR1) is extended to include 10% network-trust weight, making recommendations resistant to gaming by isolated cliques.

#### Acceptance criteria

- Score is bounded 0–100.
- New families inherit a neutral score.
- Recomputation cadence is documented.
- Recommendation reasons may now cite “trusted by families with strong reputation in your network.”

## 6. Neo4j Data Model Requirements

### 6.1 Required Node Labels

The system shall use the following Neo4j nodes:

- AuthAccount
- Family
- Task
- Category
- Region
- SupportInteraction
- Feedback

### 6.2 AuthAccount Node

#### Purpose

Stores login credentials.

#### Required properties

- accountId
- email
- passwordHash
- createdAt

#### Notes

- passwordHash must never be returned to frontend.
- There is no role field — the existence of an authenticated AuthAccount is itself the authorization to act as a family on owned resources.
- AuthAccount is used for application login, not Neo4j database login.

### 6.3 Family Node

#### Purpose

Stores public family profile and trust data.

#### Required properties

- familyId
- name
- region
- verificationStatus
- trustScore
- completedHelpCount
- cancelledTaskCount
- createdAt
- lastActiveAt
- betweennessScore (cached, see GIR6)
- networkTrustScore (cached, see GIR7)

#### Default values

- verificationStatus = UNVERIFIED
- trustScore = 50
- completedHelpCount = 0
- cancelledTaskCount = 0
- betweennessScore = 0
- networkTrustScore = 50

### 6.4 Task Node

#### Purpose

Stores help offers and help requests.

#### Required properties

- taskId
- title
- description
- type
- status
- priority
- createdAt
- scheduledAt
- completedAt
- version

#### Valid type values

- OFFER
- REQUEST

#### Valid status values

- OPEN
- IN_PROGRESS
- COMPLETED
- CANCELLED
- EXPIRED

### 6.5 Category Node

#### Purpose

Represents support category.

#### Required properties

- categoryId
- name

#### Initial categories

- Tutoring
- Childcare
- Transportation
- Elder Care
- Household Assistance
- Emergency Support

### 6.6 Region Node

#### Purpose

Represents community-level location.

#### Required properties

- regionId
- name
- city

#### Example regions

- Khalifa City
- Al Reem Island
- Mussafah
- Baniyas
- Mohammed Bin Zayed City
- Al Nahyan

Exact home addresses shall not be stored.

### 6.7 SupportInteraction Node

#### Purpose

Stores the official record of a task interaction.

#### Required properties

- interactionId
- status
- acceptedAt
- completedAt
- cancelledAt

#### Valid status values

- IN_PROGRESS
- COMPLETED
- CANCELLED

### 6.8 Feedback Node

#### Purpose

Stores feedback submitted after completed tasks.

#### Required properties

- feedbackId
- rating
- comment
- reliabilityRating
- communicationRating
- createdAt

### 6.9 Required Relationships

The system shall implement the following graph relationships:

`(:AuthAccount)-[:OWNS_PROFILE]->(:Family)`

`(:Family)-[:LOCATED_IN]->(:Region)`

`(:Family)-[:POSTED]->(:Task)`

`(:Family)-[:ACCEPTED { acceptedAt }]->(:Task)`

`(:Task)-[:IN_CATEGORY]->(:Category)`

`(:Task)-[:LOCATED_IN]->(:Region)`

`(:Task)-[:RESULTED_IN]->(:SupportInteraction)`

`(:Family)-[:REQUESTER_IN]->(:SupportInteraction)`

`(:Family)-[:HELPER_IN]->(:SupportInteraction)`

`(:Family)-[:WROTE]->(:Feedback)`

`(:Feedback)-[:REVIEWS]->(:Family)`

`(:Feedback)-[:FOR_INTERACTION]->(:SupportInteraction)`

`(:Family)-[:HELPED { interactionId, taskId, category, rating, date }]->(:Family)`

### 6.10 Neo4j Constraints

The system shall create unique constraints for:

- AuthAccount.accountId
- AuthAccount.email
- Family.familyId
- Task.taskId
- Category.name
- Region.name
- SupportInteraction.interactionId
- Feedback.feedbackId

### 6.11 Neo4j Indexes

The system shall create indexes for:

- Task.status
- Task.type
- Task.createdAt
- Family.region
- Family.trustScore

Optional:

- Full-text index on Task.title and Task.description

## 7. API Requirements

### 7.1 Authentication APIs

`POST /api/auth/register`

`POST /api/auth/login`

`GET /api/auth/me`

### 7.2 Family APIs

`GET /api/families/{familyId}`

`PUT /api/families/{familyId}`

`GET /api/families/{familyId}/trust-score`

`GET /api/families/{familyId}/interactions`

`GET /api/families/{familyId}/feedback`

### 7.3 Task APIs

`GET /api/tasks`

`POST /api/tasks`

`GET /api/tasks/{taskId}`

`POST /api/tasks/{taskId}/accept`

`POST /api/tasks/{taskId}/complete`

`POST /api/tasks/{taskId}/cancel`

### 7.4 Feedback APIs

`POST /api/interactions/{interactionId}/feedback`

### 7.5 Recommendation and Graph APIs

`GET /api/tasks/{taskId}/recommended-families`

`GET /api/families/{familyId}/suggested-connections`

`GET /api/families/{familyId}/degree-of-separation/{otherFamilyId}`

`GET /api/families/{familyId}/expertise?category={categoryName}`

`GET /api/families/{familyId}/network`

`GET /api/families/{familyId}/network-trust`

`GET /api/families/community-connectors?limit={n}`

### 7.6 Statistics APIs

`GET /api/statistics/community`

## 8. Security Requirements

### SR1 — Password Protection

All passwords hashed with BCrypt; plaintext never stored; hashes never returned in responses.

### SR2 — JWT Authentication

Stateless JWT issued on login, required on all protected endpoints, identifies the authenticated family.

### SR3 — Ownership Authorization

All non-public actions are gated by ownership / participation checks performed in the service layer:

- A family can update only its own profile.
- A family cannot accept its own task.
- Only the task creator can cancel an OPEN task.
- Only task participants (requester or helper) can complete a task.
- Only participants in a completed SupportInteraction can submit feedback for it.
- A reviewer cannot review themselves.

### SR4 — Input Validation

The system shall validate all user input. Validation must include:

- Email format
- Password length
- Required fields
- Task title length
- Task description length
- Valid task type
- Valid task status
- Valid category
- Valid region
- Rating range 1 to 5
- Comment length

### SR5 — Sensitive Data Protection

The system shall never expose:

- passwordHash
- JWT secret
- Neo4j username
- Neo4j password
- Neo4j internal element IDs
- Backend environment variables

### SR6 — Neo4j Access Security

- The system shall use Neo4j database credentials only inside the Spring Boot backend.
- Frontend must not connect directly to Neo4j.
- Frontend must not contain Neo4j URI, username, or password.
- Spring Boot must act as the controlled access layer.

## 9. Concurrency Requirements

### CR1 — Single Acceptance Guarantee

The system shall guarantee that only one family can accept a task.

#### Required implementation approach

Task acceptance must be implemented as a single conditional Cypher query inside a Spring @Transactional service. The query must check that task status is OPEN and that the current family is not the poster, then atomically: set task status to IN_PROGRESS, increment Task.version (optimistic-locking semantics), create the ACCEPTED relationship, and create the SupportInteraction node — all in the same transaction.

#### Acceptance criteria

If 20 users attempt to accept the same task simultaneously:

- 1 request succeeds.
- 19 requests fail with controlled error.
- Final database state has one ACCEPTED relationship.
- Final task status is IN_PROGRESS.
- Task.version has incremented exactly once.

### CR2 — Duplicate Feedback Prevention

The system shall prevent duplicate feedback from the same reviewer for the same interaction.

#### Acceptance criteria

- First feedback submission succeeds.
- Second feedback submission from same reviewer for same interaction fails.

### CR3 — Consistent Task State Transitions

The system shall enforce valid task status transitions.

#### Allowed transitions

- OPEN → IN_PROGRESS
- OPEN → CANCELLED
- IN_PROGRESS → COMPLETED
- IN_PROGRESS → CANCELLED

#### Disallowed transitions

- COMPLETED → OPEN
- CANCELLED → OPEN
- COMPLETED → CANCELLED
- OPEN → COMPLETED

## 10. Scalability Requirements

### SCR1 — Bounded Graph Traversal

All graph traversal queries must be bounded.

Examples:

`[:HELPED*..2]`

`[:HELPED*..5]`

Unbounded traversal is not allowed:

`[:HELPED*]`

### SCR2 — Pagination and Limits

The system shall use result limits or pagination for:

- Task feed
- Feedback list
- Recommended families
- Suggested connections
- Network graph endpoint

### SCR3 — Indexed Queries

The system shall use Neo4j indexes for frequently queried properties:

- Task.status
- Task.type
- Task.createdAt
- Family.region
- Family.trustScore

### SCR4 — Cached Trust Score

- The system shall cache calculated trust score on the Family node.
- Trust score is recalculated after feedback or relevant task update.
- Dashboard does not recalculate trust from scratch on every request.
- Betweenness and network-trust scores are likewise cached on the Family node and recomputed on a scheduled cadence (see GIR6, GIR7).

### SCR5 — DTO-Based API Responses

The system shall return DTOs rather than full Neo4j entity graphs. This prevents:

- Large nested graph responses
- Accidental exposure of sensitive data
- Circular JSON serialization issues
- Slow frontend responses

## 11. Frontend Requirements

### 11.1 Frontend Runtime Environment

- Runs as a Vite + React dev server on http://localhost:5173.
- Communicates with the Spring Boot backend on http://localhost:8080 via fetch / axios.
- JWT is attached to every protected request via the Authorization: Bearer <token> header.
- JWT is held in memory (or sessionStorage) for the dev demo; production hardening is out of scope.
- No service worker, no PWA, no build / deploy pipeline required.

### 11.2 Required Pages

The frontend shall include:

- Login page
- Registration page
- Dashboard page
- Task feed page
- Create task page / modal
- Task details page
- Family profile page
- Feedback form
- Recommendations page / section
- Network visualization page / section

### 11.3 Dashboard Requirements

The dashboard shall show:

- Current logged-in family
- Trust score
- Open help requests
- Open help offers
- Recommended tasks or families
- Recent interactions

### 11.4 Task Feed Requirements

The task feed shall allow users to:

- View open tasks
- Filter by OFFER or REQUEST
- Filter by category
- Filter by region
- Open task details
- Accept available task

### 11.5 Family Profile Requirements

The profile page shall show:

- Family name
- Region
- Trust score
- Completed help count
- Feedback received
- Badges or reputation level (Community Connector tier, optional)

### 11.6 Recommendation UI Requirements

The frontend shall display recommended families with explanations.

Example:

`Al Noor Family`

`Trust Score: 88`

`Common Neighbors: 3`

`Category Experience: 5 tutoring tasks`

`Reason: High trust score and strong tutoring history in your region.`

## 12. Backend Requirements

### 12.1 Layered Architecture Pattern

Every domain in the backend shall follow a four-layer pattern, mirroring the structure used in Lab 3.2:

1. Controller layer — @RestController classes with class-level @RequestMapping defining a base path. Methods use @GetMapping, @PostMapping, @PutMapping, @DeleteMapping and return ResponseEntity<T>. Controllers contain no business logic; they translate HTTP into service calls and service results into HTTP responses.

2. Service interface — A Java interface declaring the public contract of the domain (e.g. TaskService). All method signatures live here. This is what controllers depend on.

3. Service implementation — A @Service-annotated class implementing the interface (e.g. TaskServiceImpl implements TaskService). All business logic, ownership checks, and @Transactional boundaries live here. Methods are annotated @Override.

4. Repository layer — A @Repository interface extending Neo4jRepository<T, String>. Derived queries (e.g. findByStatus) and custom @Query Cypher queries (for atomic accept, HELPED creation, recommendations, shortest path, etc.) are declared here. The actual betweenness and PageRank algorithms are implemented in the service layer in plain Java; repositories handle only the Cypher reads / writes that feed those algorithms.

Example chain for “create task”: TaskController.createTask() → TaskService.createTask() → TaskServiceImpl.createTask() → TaskRepository.save().

### 12.2 Package Structure

`com.familyhelpuae`

`├── auth (AuthController, AuthService, AuthServiceImpl, AuthAccountRepository)`

`├── family (FamilyController, FamilyService, FamilyServiceImpl, FamilyRepository)`

`├── task (TaskController, TaskService, TaskServiceImpl, TaskRepository)`

`├── interaction (InteractionService, InteractionServiceImpl, InteractionRepository)`

`├── feedback (FeedbackController, FeedbackService, FeedbackServiceImpl, FeedbackRepository)`

`├── trust (TrustScoreService, TrustScoreServiceImpl)`

`├── recommendation (RecommendationController, RecommendationService,`

`│ RecommendationServiceImpl, RecommendationRepository)`

`├── centrality (CentralityService, CentralityServiceImpl — betweenness & PageRank)`

`├── statistics (StatisticsController, StatisticsService, StatisticsServiceImpl)`

`├── security (JwtFilter, JwtUtil, SecurityConfig, BCrypt config)`

`├── config (Neo4jConfig, OpenApiConfig, CorsConfig)`

`└── common (DTOs, exceptions, validators, GlobalExceptionHandler)`

### 12.3 Required Service Classes

Each domain provides a Service interface and a ServiceImpl class:

| Interface | Implementation |
| --- | --- |
| AuthService | AuthServiceImpl |
| FamilyService | FamilyServiceImpl |
| TaskService | TaskServiceImpl |
| InteractionService | InteractionServiceImpl |
| FeedbackService | FeedbackServiceImpl |
| TrustScoreService | TrustScoreServiceImpl |
| RecommendationService | RecommendationServiceImpl |
| CentralityService | CentralityServiceImpl |
| StatisticsService | StatisticsServiceImpl |

AdminService is removed. No admin-only operations exist in the system.

### 12.4 Required Repository Classes

The backend should include Neo4j repositories such as:

- AuthAccountRepository
- FamilyRepository
- TaskRepository
- FeedbackRepository
- InteractionRepository
- RecommendationRepository

Custom Cypher queries should be used for:

- Task acceptance (status guard + version increment)
- Feedback submission
- HELPED relationship creation
- Trust score calculation
- Recommendations
- Degree of separation
- Network visualization
- HELPED subgraph extraction for betweenness computation (GIR6)
- HELPED subgraph extraction for PageRank computation (GIR7)
- Bulk update of betweennessScore and networkTrustScore on Family nodes
- Community connector ranking query

The actual centrality and PageRank algorithms live in the service layer (CentralityServiceImpl), not in the repositories.

### 12.5 Required Controller Classes

| Controller | Base Path | Responsibilities |
| --- | --- | --- |
| AuthController | /api/auth | register, login, current user |
| FamilyController | /api/families | profile view / update, trust score, feedback list, interactions |
| TaskController | /api/tasks | task CRUD, accept, complete, cancel, filtered feed |
| FeedbackController | /api/interactions | feedback submission |
| RecommendationController | /api/families, /api/tasks | recommendations, suggested connections, degree of separation, expertise, network, centrality, network-trust |
| StatisticsController | /api/statistics | community statistics dashboard |

## 13. Swagger Documentation Requirements

The system shall provide Swagger documentation for all APIs. Swagger must include:

- Endpoint descriptions
- Request body examples
- Response examples
- Authentication requirements
- Error responses
- Trust score formula explanation
- Recommendation logic explanation

Swagger URL should be available at:

`/swagger-ui.html`

or:

`/swagger-ui/index.html`

## 14. Testing Requirements

### 14.1 Functional Test Cases

The system shall be tested for:

- Registration success
- Duplicate email rejection
- Login success
- Login failure
- JWT protected endpoint access
- Update profile — success for owner
- Update profile — rejected for non-owner
- Create task
- View task feed
- Accept task
- Reject self-acceptance
- Reject accepting already accepted task
- Cancel own OPEN task — success
- Cancel another family's task — rejected
- Cancel COMPLETED task — rejected
- Complete task
- Submit feedback
- Reject duplicate feedback
- Trust score update
- Recommended families retrieval
- Degree of separation retrieval
- Network graph retrieval
- Access public community statistics (success for authenticated user)
- Betweenness endpoint returns ranked list
- Network-trust score updates after feedback

### 14.2 Concurrency Test Cases

The system shall include a concurrency simulation.

#### Test environment

The 20-concurrent-accept simulation is executed against the local backend (http://localhost:8080) using a multi-threaded test client (e.g., JMeter, k6, or a Java ExecutorService test). All 20 simulated clients originate from the same host as the server.

#### Minimum required test

20 concurrent users attempt to accept the same OPEN task.

#### Expected result

- 1 success
- 19 controlled failures
- Only one ACCEPTED relationship in Neo4j
- Only one SupportInteraction created
- Task status is IN_PROGRESS
- Task.version has incremented exactly once

#### Better additional test

100 concurrent GET requests to /api/tasks.

#### Expected result

- All requests complete successfully.
- No server crash.
- Acceptable average response time.

### 14.3 Report Testing Table

The report shall include a table:

| Test Case | Input | Expected Result | Actual Result |
| --- | --- | --- | --- |
| Register family | Valid details | Account created | Account created |
| Duplicate email | Existing email | Rejected | Rejected |
| Accept task | Valid family | Task IN_PROGRESS | Task IN_PROGRESS |
| Concurrent accept | 20 users | 1 success only | 1 success only |
| Submit feedback | Completed task | Feedback saved | Feedback saved |

## 15. Report Requirements

The final report shall include:

- Project overview
- System architecture
- REST API documentation or Swagger screenshots
- Neo4j data model
- Explanation of graph-powered innovation
- Authentication and authorization design
- Trust score formula
- Recommendation formula
- Concurrency handling explanation
- Scalability discussion
- Testing results
- Simulation results
- Limitations
- Future improvements
- Self-evaluation

## 16. Data Privacy Requirements

The system shall protect family privacy.

#### Required privacy rules

- Exact home addresses must not be stored.
- Only region / community-level location is stored.
- Password hashes are never exposed.
- JWT secrets are stored only in backend configuration.
- Neo4j credentials are stored only as environment variables.
- Frontend never accesses Neo4j directly.

## 17. Deployment and Configuration Requirements

### 17.1 Backend Configuration

Spring Boot shall be configurable using environment variables:

`spring.neo4j.uri=${NEO4J_URI}`

`spring.neo4j.authentication.username=${NEO4J_USERNAME}`

`spring.neo4j.authentication.password=${NEO4J_PASSWORD}`

`jwt.secret=${JWT_SECRET}`

### 17.2 CORS Requirement

The backend shall allow CORS requests from http://localhost:5173 only. Allowed methods: GET, POST, PUT, DELETE. Allowed headers include Authorization and Content-Type. Credentials mode is enabled to permit JWT-bearing requests. Production CORS hardening is explicitly out of scope.

## 18. Minimum Viable Delivery Scope

The minimum complete version must include:

- Register / login
- JWT security
- Family profile
- Create task
- View task feed
- Accept task
- Complete task
- Submit feedback
- Trust score
- HELPED relationship in Neo4j
- Recommended families endpoint
- Swagger documentation
- Basic React frontend (running on http://localhost:5173)
- Concurrency test
- Report and video

All deliverables run on localhost; no cloud deployment is required for the demo.

## 19. Bonus Delivery Scope

The innovation bonus version should include:

- Neo4j graph model
- HELPED trust graph
- Smart recommendation engine
- Common neighbors
- Degree of separation
- Category expertise
- Explainable recommendation reasons
- Betweenness centrality — community connectors
- PageRank network trust
- Community Connector badge tier (Gold / Silver / Bronze) surfaced in the UI

This is enough to demonstrate creativity without going beyond the assignment.

## 20. Final Requirement Statement

The final system shall be a secure, distributed, RESTful Spring Boot application with a React frontend and Neo4j AuraDB Free graph persistence layer. Families shall register, authenticate, create help offers / requests, accept and complete tasks, submit feedback, and build trust through completed interactions. The Neo4j graph shall be used not only for storage but also for innovation features: trust scoring, recommendations, common-neighbor discovery, degree-of-separation analysis, network visualization, betweenness-centrality based community-connector identification, and PageRank-based network trust propagation — all implemented in application code without reliance on the GDS plugin. The system shall include concurrency protection (status guard plus optimistic-locking version increment), Swagger documentation, testing evidence, and a final report discussing scalability, limitations, and improvements. The full stack runs on the developer's local machine for the assignment demo, with no production deployment in scope.
