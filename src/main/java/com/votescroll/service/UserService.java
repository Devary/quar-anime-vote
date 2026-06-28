package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject TokenService tokenService;

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (req.username == null || req.username.isBlank())
            throw bad("Username is required");
        if (req.email == null || req.email.isBlank())
            throw bad("Email is required");
        if (req.password == null || req.password.length() < 8)
            throw bad("Password must be at least 8 characters");
        if (!req.password.equals(req.confirmPassword))
            throw bad("Passwords do not match");
        if (AppUser.existsByUsername(req.username))
            throw conflict("Username already taken");
        if (AppUser.existsByEmail(req.email))
            throw conflict("Email already registered");

        AppRole userRole = AppRole.findById("USER");
        if (userRole == null)
            throw new WebApplicationException("USER role not configured", 500);

        AppUser user = new AppUser();
        user.username     = req.username.trim();
        user.email        = req.email.trim().toLowerCase();
        user.passwordHash = PasswordUtil.hash(req.password);
        user.roles        = new HashSet<>(Set.of(userRole));
        user.persist();

        return tokenService.issueTokens(user);
    }

    public LoginResponse login(String username, String password) {
        AppUser user = AppUser.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED));
        if (!PasswordUtil.verify(password, user.passwordHash))
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        return tokenService.issueTokens(user);
    }

    public LoginResponse refresh(String refreshToken) {
        AppUser user = tokenService.verifyRefreshAndGetUser(refreshToken);
        return tokenService.issueTokens(user);
    }

    public UsernameAvailabilityResponse checkAvailability(String username) {
        boolean available = !AppUser.existsByUsername(username);
        return new UsernameAvailabilityResponse(username, available,
                available ? "Available" : "Username is already taken");
    }

    // ── User profile ──────────────────────────────────────────────────────────

    public UserDto getProfile(String userId) {
        AppUser user = AppUser.findById(userId);
        if (user == null) throw new NotFoundException("User not found");
        return UserDto.from(user);
    }

    @Transactional
    public UserDto updateProfile(String userId, UserUpdateDto dto) {
        AppUser user = AppUser.findById(userId);
        if (user == null) throw new NotFoundException("User not found");

        if (dto.email != null && !dto.email.isBlank()) {
            String normalized = dto.email.trim().toLowerCase();
            if (!normalized.equals(user.email) && AppUser.existsByEmail(normalized))
                throw conflict("Email already in use");
            user.email = normalized;
        }
        if (dto.profilePicture != null)
            user.profilePicture = dto.profilePicture.isBlank() ? null : dto.profilePicture;

        return UserDto.from(user);
    }

    // ── Admin — Users ─────────────────────────────────────────────────────────

    public List<UserDto> listAllUsers() {
        return AppUser.<AppUser>listAll(io.quarkus.panache.common.Sort.by("createdAt").descending())
                .stream().map(UserDto::from).collect(Collectors.toList());
    }

    @Transactional
    public UserDto adminUpdateUser(String userId, AdminUserUpdateDto dto) {
        AppUser user = AppUser.findById(userId);
        if (user == null) throw new NotFoundException("User not found");

        if (dto.email != null && !dto.email.isBlank())
            user.email = dto.email.trim().toLowerCase();

        if (dto.profilePicture != null)
            user.profilePicture = dto.profilePicture.isBlank() ? null : dto.profilePicture;

        if (dto.roleIds != null) {
            Set<AppRole> roles = dto.roleIds.stream()
                    .map(id -> AppRole.<AppRole>findById(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.roles = roles;
        }
        return UserDto.from(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        AppUser user = AppUser.findById(userId);
        if (user == null) throw new NotFoundException("User not found");
        Vote.delete("userId", userId);
        MultiPollVote.delete("userId", userId);
        user.roles.clear();
        user.delete();
    }

    // ── Admin — Roles ─────────────────────────────────────────────────────────

    public List<RoleDto> listAllRoles() {
        return AppRole.<AppRole>listAll(io.quarkus.panache.common.Sort.by("id"))
                .stream().map(r -> new RoleDto(r.id, r.name, r.description))
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDto createRole(RoleCreateDto dto) {
        if (dto.id == null || dto.id.isBlank()) throw bad("Role ID is required");
        String id = dto.id.trim().toUpperCase();
        if (AppRole.findById(id) != null) throw conflict("Role already exists: " + id);
        AppRole role = AppRole.builder()
                .id(id)
                .name(dto.name != null ? dto.name : id)
                .description(dto.description)
                .build();
        role.persist();
        return new RoleDto(role.id, role.name, role.description);
    }

    @Transactional
    public void deleteRole(String roleId) {
        AppRole role = AppRole.findById(roleId);
        if (role == null) throw new NotFoundException("Role not found: " + roleId);
        // Remove from all users first
        AppUser.<AppUser>listAll().forEach(u -> u.roles.remove(role));
        role.delete();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static WebApplicationException bad(String msg) {
        return new WebApplicationException(msg, Response.Status.BAD_REQUEST);
    }

    private static WebApplicationException conflict(String msg) {
        return new WebApplicationException(msg, Response.Status.CONFLICT);
    }
}
