package com.votescroll.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String subject,
        String username,
        String email,
        List<String> roles
) {}
