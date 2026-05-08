package com.familyhelpuae.feedback;

import com.familyhelpuae.common.dto.FeedbackRequest;

public interface FeedbackService {
    void submitFeedback(String interactionId, String email, FeedbackRequest request);
}
