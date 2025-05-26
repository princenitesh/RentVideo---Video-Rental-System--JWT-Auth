package com.rentvideo.rentvideo.service;

import com.rentvideo.rentvideo.dto.JwtResponse;
import com.rentvideo.rentvideo.dto.LoginRequest;
import com.rentvideo.rentvideo.dto.RegisterRequest;
import com.rentvideo.rentvideo.dto.UserResponse;

public interface AuthService {
    UserResponse registerUser(RegisterRequest registerRequest);
    JwtResponse authenticateUser(LoginRequest loginRequest);
}