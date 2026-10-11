package com.tunnelscope.backend.service;

import com.tunnelscope.backend.dto.LoginRequest;
import com.tunnelscope.backend.dto.LoginResponse;
import com.tunnelscope.backend.model.User;
import com.tunnelscope.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new LoginResponse(
                    false,
                    "Invalid email or password",
                    null,
                    null,
                    null,
                    null
            );
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return new LoginResponse(
                    false,
                    "Invalid email or password",
                    null,
                    null,
                    null,
                    null
            );
        }

        return new LoginResponse(
                true,
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}