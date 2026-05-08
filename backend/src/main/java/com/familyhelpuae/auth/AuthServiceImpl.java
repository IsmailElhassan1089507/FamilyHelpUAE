package com.familyhelpuae.auth;

import com.familyhelpuae.common.dto.AuthResponse;
import com.familyhelpuae.common.dto.LoginRequest;
import com.familyhelpuae.common.dto.RegisterRequest;
import com.familyhelpuae.common.dto.UserDTO;
import com.familyhelpuae.family.Family;
import com.familyhelpuae.region.Region;
import com.familyhelpuae.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthAccountRepository authAccountRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Region region = new Region();
        region.setRegionId(request.getRegion().toLowerCase().replace(" ", "-"));
        region.setName(request.getRegion());

        Family family = new Family();
        family.setFamilyId(UUID.randomUUID().toString());
        family.setName(request.getFamilyName());
        family.setCreatedAt(LocalDateTime.now());
        family.setLastActiveAt(LocalDateTime.now());
        family.setRegion(region);

        AuthAccount account = new AuthAccount();
        account.setAccountId(UUID.randomUUID().toString());
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setCreatedAt(LocalDateTime.now());
        account.setFamily(family);

        authAccountRepository.save(account);

        String token = jwtUtil.generateToken(account.getEmail());
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AuthAccount account = authAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(account.getEmail());
        return new AuthResponse(token);
    }

    @Override
    public UserDTO getCurrentUser(String email) {
        AuthAccount account = authAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Family family = account.getFamily();
        
        UserDTO dto = new UserDTO();
        if (family != null) {
            dto.setFamilyId(family.getFamilyId());
            dto.setFamilyName(family.getName());
            dto.setRegion(family.getRegion() != null ? family.getRegion().getName() : null);
            dto.setTrustScore(family.getTrustScore());
        }
        dto.setEmail(account.getEmail());
        
        return dto;
    }
}
