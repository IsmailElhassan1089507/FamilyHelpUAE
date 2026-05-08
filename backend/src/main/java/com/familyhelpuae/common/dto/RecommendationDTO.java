package com.familyhelpuae.common.dto;

import lombok.Data;

@Data
public class RecommendationDTO {
    private String familyId;
    private String familyName;
    private String region;
    private int trustScore;
    private long categoryExperience;
    private long commonNeighbors;
    private double recommendationScore;
    private String reason;
}
