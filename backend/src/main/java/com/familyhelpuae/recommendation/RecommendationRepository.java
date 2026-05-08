package com.familyhelpuae.recommendation;

import com.familyhelpuae.family.Family;
import com.familyhelpuae.recommendation.projection.CategoryExpertiseProjection;
import com.familyhelpuae.recommendation.projection.CommonNeighborProjection;
import com.familyhelpuae.recommendation.projection.GraphEdgeProjection;
import com.familyhelpuae.recommendation.projection.GraphNodeProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends Neo4jRepository<Family, String> {

    @Query("MATCH (f1:Family {familyId: $familyId})-[:HELPED]-(neighbor:Family)-[:HELPED]-(f2:Family) " +
           "WHERE f1 <> f2 " +
           "RETURN f2.familyId AS candidateFamilyId, f2.name AS candidateFamilyName, f2.trustScore AS trustScore, count(DISTINCT neighbor) AS commonNeighborCount " +
           "ORDER BY commonNeighborCount DESC LIMIT 10")
    List<CommonNeighborProjection> findCommonNeighbors(@Param("familyId") String familyId);

    @Query("MATCH p=shortestPath((f1:Family {familyId: $sourceFamilyId})-[:HELPED*..5]-(f2:Family {familyId: $targetFamilyId})) " +
           "RETURN length(p)")
    Optional<Integer> findDegreeOfSeparation(@Param("sourceFamilyId") String sourceFamilyId, @Param("targetFamilyId") String targetFamilyId);

    @Query("MATCH (f:Family {familyId: $familyId})-[(h:HELPED)]->(:Family) " +
           "WHERE h.category = $category " +
           "RETURN count(h) AS completedTaskCount, avg(h.rating) AS averageRating")
    CategoryExpertiseProjection getCategoryExpertise(@Param("familyId") String familyId, @Param("category") String category);

    @Query("MATCH (f1:Family)-[h:HELPED]->(f2:Family) " +
           "RETURN f1.familyId AS sourceId, f2.familyId AS targetId, h.rating AS weight")
    List<GraphEdgeProjection> getHelpedGraphEdges();

    @Query("MATCH (f:Family) RETURN f.familyId AS familyId, f.name AS name, f.region AS region")
    List<GraphNodeProjection> getAllFamilies();
    
}
