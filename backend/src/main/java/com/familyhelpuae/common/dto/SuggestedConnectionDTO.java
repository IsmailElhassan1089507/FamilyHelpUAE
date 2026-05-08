package com.familyhelpuae.common.dto;

import lombok.Data;

@Data
public class SuggestedConnectionDTO {
    private String candidateFamilyId;
    private String candidateFamilyName;
    private int trustScore;
    private long commonNeighborCount;
}
