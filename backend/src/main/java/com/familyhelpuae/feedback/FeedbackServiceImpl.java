package com.familyhelpuae.feedback;

import com.familyhelpuae.auth.AuthAccount;
import com.familyhelpuae.auth.AuthAccountRepository;
import com.familyhelpuae.common.dto.FeedbackRequest;
import com.familyhelpuae.family.Family;
import com.familyhelpuae.family.FamilyRepository;
import com.familyhelpuae.interaction.InteractionRepository;
import com.familyhelpuae.interaction.SupportInteraction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final InteractionRepository interactionRepository;
    private final AuthAccountRepository authAccountRepository;
    private final FamilyRepository familyRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, 
                               InteractionRepository interactionRepository, 
                               AuthAccountRepository authAccountRepository,
                               FamilyRepository familyRepository) {
        this.feedbackRepository = feedbackRepository;
        this.interactionRepository = interactionRepository;
        this.authAccountRepository = authAccountRepository;
        this.familyRepository = familyRepository;
    }

    @Override
    @Transactional
    public void submitFeedback(String interactionId, String email, FeedbackRequest request) {
        AuthAccount account = authAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Family reviewer = account.getFamily();

        SupportInteraction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new IllegalArgumentException("Interaction not found"));

        if (!"COMPLETED".equals(interaction.getStatus())) {
            throw new IllegalStateException("Cannot submit feedback for incomplete interaction");
        }

        if (reviewer.getFamilyId().equals(request.getRevieweeFamilyId())) {
            throw new IllegalStateException("Cannot review yourself");
        }

        Family reviewee = familyRepository.findById(request.getRevieweeFamilyId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewee not found"));

        Feedback feedback = new Feedback();
        feedback.setFeedbackId(UUID.randomUUID().toString());
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setReliabilityRating(request.getReliabilityRating());
        feedback.setCommunicationRating(request.getCommunicationRating());
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setInteraction(interaction);
        feedback.setReviewedFamily(reviewee);
        
        feedbackRepository.save(feedback);
        
        // Custom query to create WROTE relationship from Reviewer to Feedback
        // For simplicity we will rely on SDN or we can add it later via Cypher
    }
}
