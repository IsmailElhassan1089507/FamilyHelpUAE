package com.familyhelpuae.recommendation.projection;

public interface CommonNeighborProjection {
    String getCandidateFamilyId();
    String getCandidateFamilyName();
    Integer getTrustScore();
    Long getCommonNeighborCount();
}
