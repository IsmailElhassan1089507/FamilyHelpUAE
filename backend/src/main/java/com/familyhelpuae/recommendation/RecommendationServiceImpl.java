package com.familyhelpuae.recommendation;

import com.familyhelpuae.auth.AuthAccount;
import com.familyhelpuae.auth.AuthAccountRepository;
import com.familyhelpuae.common.dto.RecommendationDTO;
import com.familyhelpuae.common.dto.SuggestedConnectionDTO;
import com.familyhelpuae.family.Family;
import com.familyhelpuae.family.FamilyRepository;
import com.familyhelpuae.recommendation.projection.CategoryExpertiseProjection;
import com.familyhelpuae.recommendation.projection.CommonNeighborProjection;
import com.familyhelpuae.task.Task;
import com.familyhelpuae.task.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final TaskRepository taskRepository;
    private final FamilyRepository familyRepository;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository,
                                     AuthAccountRepository authAccountRepository,
                                     TaskRepository taskRepository,
                                     FamilyRepository familyRepository) {
        this.recommendationRepository = recommendationRepository;
        this.authAccountRepository = authAccountRepository;
        this.taskRepository = taskRepository;
        this.familyRepository = familyRepository;
    }

    private Family getFamilyByEmail(String email) {
        return authAccountRepository.findByEmail(email)
                .map(AuthAccount::getFamily)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public List<RecommendationDTO> getRecommendedFamilies(String taskId, String email) {
        Family requester = getFamilyByEmail(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        String categoryName = task.getCategory() != null ? task.getCategory().getName() : "";
        String requesterRegion = requester.getRegion() != null ? requester.getRegion().getName() : "";

        List<Family> allFamilies = familyRepository.findAll();
        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (Family candidate : allFamilies) {
            if (candidate.getFamilyId().equals(requester.getFamilyId())) {
                continue; // Cannot recommend self
            }

            CategoryExpertiseProjection expertise = recommendationRepository.getCategoryExpertise(candidate.getFamilyId(), categoryName);
            long expertiseCount = expertise != null ? expertise.getCompletedTaskCount() : 0;

            Optional<Integer> degree = recommendationRepository.findDegreeOfSeparation(requester.getFamilyId(), candidate.getFamilyId());
            boolean isSameRegion = candidate.getRegion() != null && candidate.getRegion().getName().equals(requesterRegion);

            List<CommonNeighborProjection> commonNeighbors = recommendationRepository.findCommonNeighbors(requester.getFamilyId());
            long commonNeighborCount = commonNeighbors.stream()
                    .filter(cn -> cn.getCandidateFamilyId().equals(candidate.getFamilyId()))
                    .map(CommonNeighborProjection::getCommonNeighborCount)
                    .findFirst().orElse(0L);

            double score = calculateScore(candidate, expertiseCount, isSameRegion, commonNeighborCount);

            RecommendationDTO dto = new RecommendationDTO();
            dto.setFamilyId(candidate.getFamilyId());
            dto.setFamilyName(candidate.getName());
            dto.setRegion(candidate.getRegion() != null ? candidate.getRegion().getName() : "");
            dto.setTrustScore(candidate.getTrustScore());
            dto.setCategoryExperience(expertiseCount);
            dto.setCommonNeighbors(commonNeighborCount);
            dto.setRecommendationScore(score);
            dto.setReason(generateReason(score, isSameRegion, expertiseCount, commonNeighborCount));

            recommendations.add(dto);
        }

        recommendations.sort((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()));
        return recommendations.stream().limit(10).collect(Collectors.toList());
    }

    private double calculateScore(Family candidate, long expertiseCount, boolean isSameRegion, long commonNeighborCount) {
        double trustWeight = candidate.getTrustScore() * 0.30;
        double expertiseWeight = Math.min(expertiseCount * 10, 100) * 0.25;
        double regionWeight = isSameRegion ? 100 * 0.15 : 0;
        double neighborWeight = Math.min(commonNeighborCount * 20, 100) * 0.15;
        double networkTrustWeight = candidate.getNetworkTrustScore() * 0.10;
        double reliabilityWeight = Math.max(100 - (candidate.getCancelledTaskCount() * 10), 0) * 0.05;

        return trustWeight + expertiseWeight + regionWeight + neighborWeight + networkTrustWeight + reliabilityWeight;
    }

    private String generateReason(double score, boolean isSameRegion, long expertise, long commonNeighbors) {
        StringBuilder reason = new StringBuilder("Recommended because ");
        if (isSameRegion) reason.append("they are in your region, ");
        if (expertise > 0) reason.append("have ").append(expertise).append(" tasks of experience in this category, ");
        if (commonNeighbors > 0) reason.append("and you share ").append(commonNeighbors).append(" common connections.");
        return reason.toString();
    }

    @Override
    public List<SuggestedConnectionDTO> getSuggestedConnections(String email) {
        Family family = getFamilyByEmail(email);
        List<CommonNeighborProjection> neighbors = recommendationRepository.findCommonNeighbors(family.getFamilyId());

        return neighbors.stream().map(cn -> {
            SuggestedConnectionDTO dto = new SuggestedConnectionDTO();
            dto.setCandidateFamilyId(cn.getCandidateFamilyId());
            dto.setCandidateFamilyName(cn.getCandidateFamilyName());
            dto.setTrustScore(cn.getTrustScore());
            dto.setCommonNeighborCount(cn.getCommonNeighborCount());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDegreeOfSeparation(String sourceEmail, String targetFamilyId) {
        Family source = getFamilyByEmail(sourceEmail);
        Optional<Integer> degree = recommendationRepository.findDegreeOfSeparation(source.getFamilyId(), targetFamilyId);

        Map<String, Object> result = new HashMap<>();
        result.put("sourceFamilyId", source.getFamilyId());
        result.put("targetFamilyId", targetFamilyId);
        result.put("pathExists", degree.isPresent());
        result.put("degreeOfSeparation", degree.orElse(-1));
        return result;
    }

    @Override
    public CategoryExpertiseProjection getCategoryExpertise(String email, String category) {
        Family family = getFamilyByEmail(email);
        return recommendationRepository.getCategoryExpertise(family.getFamilyId(), category);
    }

    @Override
    public Map<String, Object> getNetworkVisualization(String email) {
        // Simplified fallback as Neo4j template/cypher mapping for nodes/edges can be tricky
        // in standard Neo4jRepository without custom mappers.
        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", new ArrayList<>());
        graph.put("links", new ArrayList<>());
        return graph;
    }
}
