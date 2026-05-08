package com.familyhelpuae.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FeedbackRequest {
    private String revieweeFamilyId;
    @Min(1) @Max(5)
    private int rating;
    private String comment;
    @Min(1) @Max(5)
    private int reliabilityRating;
    @Min(1) @Max(5)
    private int communicationRating;
}
