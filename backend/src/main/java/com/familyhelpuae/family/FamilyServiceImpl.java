package com.familyhelpuae.family;

import com.familyhelpuae.auth.AuthAccount;
import com.familyhelpuae.auth.AuthAccountRepository;
import com.familyhelpuae.common.dto.UserDTO;
import com.familyhelpuae.region.Region;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final AuthAccountRepository authAccountRepository;

    public FamilyServiceImpl(FamilyRepository familyRepository, AuthAccountRepository authAccountRepository) {
        this.familyRepository = familyRepository;
        this.authAccountRepository = authAccountRepository;
    }

    @Override
    public UserDTO getFamilyProfile(String familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found"));

        UserDTO dto = new UserDTO();
        dto.setFamilyId(family.getFamilyId());
        dto.setFamilyName(family.getName());
        dto.setRegion(family.getRegion() != null ? family.getRegion().getName() : null);
        dto.setTrustScore(family.getTrustScore());
        return dto;
    }

    @Override
    @Transactional
    public UserDTO updateFamilyProfile(String familyId, String email, String name, String regionStr, String bio) {
        AuthAccount account = authAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!account.getFamily().getFamilyId().equals(familyId)) {
            throw new IllegalStateException("You can only update your own profile");
        }

        Family family = account.getFamily();
        if (name != null) family.setName(name);
        if (regionStr != null) {
            Region region = new Region();
            region.setRegionId(regionStr.toLowerCase().replace(" ", "-"));
            region.setName(regionStr);
            family.setRegion(region);
        }
        
        familyRepository.save(family);
        
        UserDTO dto = new UserDTO();
        dto.setFamilyId(family.getFamilyId());
        dto.setFamilyName(family.getName());
        dto.setRegion(family.getRegion() != null ? family.getRegion().getName() : null);
        dto.setTrustScore(family.getTrustScore());
        return dto;
    }

    @Override
    public int getTrustScore(String familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found"));
        return family.getTrustScore();
    }
}
