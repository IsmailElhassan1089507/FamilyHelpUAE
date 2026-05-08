package com.familyhelpuae.family;

import com.familyhelpuae.common.dto.UserDTO;

public interface FamilyService {
    UserDTO getFamilyProfile(String familyId);
    UserDTO updateFamilyProfile(String familyId, String email, String name, String regionStr, String bio);
    int getTrustScore(String familyId);
}
