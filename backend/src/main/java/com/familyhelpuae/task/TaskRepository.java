package com.familyhelpuae.task;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TaskRepository extends Neo4jRepository<Task, String> {

    @Query("MATCH (f:Family {familyId: $familyId}) " +
           "MATCH (t:Task {taskId: $taskId}) " +
           "MERGE (f)-[:POSTED]->(t)")
    void linkTaskToFamily(@Param("familyId") String familyId, @Param("taskId") String taskId);

    @Query("MATCH (f:Family {familyId: $familyId}) " +
           "MATCH (t:Task {taskId: $taskId}) " +
           "WHERE t.status = 'OPEN' AND NOT (f)-[:POSTED]->(t) " +
           "SET t.status = 'IN_PROGRESS', t.version = t.version + 1 " +
           "WITH f, t " +
           "CREATE (i:SupportInteraction {interactionId: $interactionId, status: 'IN_PROGRESS', acceptedAt: $now}) " +
           "CREATE (f)-[:ACCEPTED {acceptedAt: $now}]->(t) " +
           "CREATE (t)-[:RESULTED_IN]->(i) " +
           "WITH f, t, i " +
           "MATCH (poster:Family)-[:POSTED]->(t) " +
           "FOREACH (ignoreMe IN CASE WHEN t.type = 'REQUEST' THEN [1] ELSE [] END | " +
           "  CREATE (poster)-[:REQUESTER_IN]->(i) " +
           "  CREATE (f)-[:HELPER_IN]->(i) " +
           ") " +
           "FOREACH (ignoreMe IN CASE WHEN t.type = 'OFFER' THEN [1] ELSE [] END | " +
           "  CREATE (f)-[:REQUESTER_IN]->(i) " +
           "  CREATE (poster)-[:HELPER_IN]->(i) " +
           ") " +
           "RETURN count(t)")
    long atomicAcceptTask(@Param("taskId") String taskId, 
                          @Param("familyId") String familyId, 
                          @Param("interactionId") String interactionId,
                          @Param("now") LocalDateTime now);
}
