package com.familyhelpuae.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {
    private String title;
    private String description;
    private String type; // OFFER or REQUEST
    private String category;
    private String region;
    private LocalDateTime scheduledAt;
    private String priority;
}
