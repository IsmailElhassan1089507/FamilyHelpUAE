package com.familyhelpuae.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private String taskId;
    private String title;
    private String description;
    private String type;
    private String status;
    private String category;
    private String region;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private String postedByFamilyId;
    private String postedByFamilyName;
    private String acceptedByFamilyId;
    private String acceptedByFamilyName;
}
