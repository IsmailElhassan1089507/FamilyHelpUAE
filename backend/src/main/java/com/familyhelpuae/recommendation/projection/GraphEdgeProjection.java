package com.familyhelpuae.recommendation.projection;

public interface GraphEdgeProjection {
    String getSourceId();
    String getTargetId();
    Double getWeight();
}
