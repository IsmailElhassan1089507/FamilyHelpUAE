package com.familyhelpuae.recommendation;

import com.familyhelpuae.centrality.CentralityService;
import com.familyhelpuae.common.dto.RecommendationDTO;
import com.familyhelpuae.common.dto.SuggestedConnectionDTO;
import com.familyhelpuae.recommendation.projection.CategoryExpertiseProjection;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CentralityService centralityService;

    public RecommendationController(RecommendationService recommendationService, CentralityService centralityService) {
        this.recommendationService = recommendationService;
        this.centralityService = centralityService;
    }

    @GetMapping("/tasks/{taskId}/suitable-families")
    public ResponseEntity<List<RecommendationDTO>> getSuitableFamilies(@PathVariable String taskId, Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getRecommendedFamilies(taskId, authentication.getName()));
    }

    @GetMapping("/suggested-connections")
    public ResponseEntity<List<SuggestedConnectionDTO>> getSuggestedConnections(Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getSuggestedConnections(authentication.getName()));
    }

    @GetMapping("/degree-of-separation/{targetFamilyId}")
    public ResponseEntity<Map<String, Object>> getDegreeOfSeparation(@PathVariable String targetFamilyId, Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getDegreeOfSeparation(authentication.getName(), targetFamilyId));
    }

    @GetMapping("/expertise")
    public ResponseEntity<CategoryExpertiseProjection> getExpertise(@RequestParam String category, Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getCategoryExpertise(authentication.getName(), category));
    }

    @GetMapping("/network")
    public ResponseEntity<Map<String, Object>> getNetworkVisualization(Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getNetworkVisualization(authentication.getName()));
    }

    @PostMapping("/admin/compute-centrality")
    public ResponseEntity<Void> computeCentrality() {
        centralityService.computeAndCacheCentralityScores();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/compute-pagerank")
    public ResponseEntity<Void> computePageRank() {
        centralityService.computeAndCachePageRankScores();
        return ResponseEntity.ok().build();
    }
}
