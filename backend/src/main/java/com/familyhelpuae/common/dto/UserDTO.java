package com.familyhelpuae.common.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String familyId;
    private String familyName;
    private String email;
    private String region;
    private int trustScore;
}
