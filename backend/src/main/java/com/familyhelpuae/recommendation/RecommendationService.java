package com.familyhelpuae.recommendation;

import com.familyhelpuae.common.dto.RecommendationDTO;
import com.familyhelpuae.common.dto.SuggestedConnectionDTO;
import com.familyhelpuae.recommendation.projection.CategoryExpertiseProjection;

import java.util.List;
import java.util.Map;

public interface RecommendationService {
    List<RecommendationDTO> getRecommendedFamilies(String taskId, String email);
    List<SuggestedConnectionDTO> getSuggestedConnections(String email);
    Map<String, Object> getDegreeOfSeparation(String sourceEmail, String targetFamilyId);
    CategoryExpertiseProjection getCategoryExpertise(String email, String category);
    Map<String, Object> getNetworkVisualization(String email);
}
