package com.familyhelpuae.family;

import com.familyhelpuae.common.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/{familyId}")
    public ResponseEntity<UserDTO> getFamily(@PathVariable String familyId) {
        try {
            return ResponseEntity.ok(familyService.getFamilyProfile(familyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{familyId}")
    public ResponseEntity<UserDTO> updateFamily(
            @PathVariable String familyId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String bio,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(familyService.updateFamilyProfile(familyId, authentication.getName(), name, region, bio));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{familyId}/trust-score")
    public ResponseEntity<Integer> getTrustScore(@PathVariable String familyId) {
        try {
            return ResponseEntity.ok(familyService.getTrustScore(familyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
