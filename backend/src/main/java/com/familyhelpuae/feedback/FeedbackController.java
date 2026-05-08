package com.familyhelpuae.feedback;

import com.familyhelpuae.common.dto.FeedbackRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/{interactionId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable String interactionId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {
        try {
            feedbackService.submitFeedback(interactionId, authentication.getName(), request);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
