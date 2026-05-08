package com.familyhelpuae.auth;

import com.familyhelpuae.common.dto.AuthResponse;
import com.familyhelpuae.common.dto.LoginRequest;
import com.familyhelpuae.common.dto.RegisterRequest;
import com.familyhelpuae.common.dto.UserDTO;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDTO getCurrentUser(String email);
}
