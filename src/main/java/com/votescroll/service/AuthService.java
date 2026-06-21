package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.AppUser;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.username == null || req.username.isBlank()) throw new BadRequestException("Username required");
        if (req.email == null || req.email.isBlank()) throw new BadRequestException("Email required");
        if (req.password == null || req.password.length() < 4) throw new BadRequestException("Password must be at least 4 characters");
        if (AppUser.findByUsername(req.username).isPresent()) throw new ClientErrorException("Username already taken", 409);
        if (AppUser.findByEmail(req.email).isPresent()) throw new ClientErrorException("Email already registered", 409);

        AppUser user = AppUser.builder()
            .username(req.username)
            .email(req.email)
            .passwordHash(BcryptUtil.bcryptHash(req.password))
            .build();
        user.persist();
        log.info("Registered user: {}", req.username);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = AppUser.findByUsername(req.username)
            .orElseThrow(() -> new ClientErrorException("Invalid credentials", 401));
        if (!BcryptUtil.matches(req.password, user.passwordHash)) {
            throw new ClientErrorException("Invalid credentials", 401);
        }
        log.info("Login: {}", req.username);
        return buildResponse(user);
    }

    private AuthResponse buildResponse(AppUser user) {
        String token = Jwt.issuer("anime-vote")
            .subject(user.id.toString())
            .claim("username", user.username)
            .groups(user.role.name())
            .expiresIn(Duration.ofDays(1))
            .sign();
        return AuthResponse.builder()
            .token(token)
            .userId(user.id)
            .username(user.username)
            .role(user.role.name())
            .build();
    }
}
