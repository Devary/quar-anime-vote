package com.votescroll.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.votescroll.config.KeycloakAuthConfig;
import com.votescroll.dto.LoginResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class KeycloakPasswordGrantService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final KeycloakAuthConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public KeycloakPasswordGrantService(KeycloakAuthConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public LoginResponse login(String username, String password) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.tokenUrl()))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildForm(username, password)))
                .build();
        return exchange(request, username);
    }

    public LoginResponse refresh(String refreshToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.tokenUrl()))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(buildRefreshForm(refreshToken)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new WebApplicationException("Refresh token expired or invalid", Response.Status.UNAUTHORIZED);
            }
            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), MAP_TYPE);
            return mapToLoginResponse(tokenResponse, null);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to parse Keycloak refresh response", e, Response.Status.BAD_GATEWAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Interrupted while refreshing token", e, Response.Status.BAD_GATEWAY);
        }
    }

    private LoginResponse exchange(HttpRequest request, String fallbackUsername) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new WebApplicationException(response.body(), response.statusCode());
            }
            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), MAP_TYPE);
            return mapToLoginResponse(tokenResponse, fallbackUsername);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to parse Keycloak response", e, Response.Status.BAD_GATEWAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Interrupted while calling Keycloak", e, Response.Status.BAD_GATEWAY);
        }
    }

    private LoginResponse mapToLoginResponse(Map<String, Object> tokenResponse, String fallbackUsername) throws IOException {
        String accessToken = stringValue(tokenResponse.get("access_token"))
                .orElseThrow(() -> new WebApplicationException("Keycloak response missing access_token", Response.Status.BAD_GATEWAY));
        Map<String, Object> claims = parseJwtPayload(accessToken);
        List<String> roles = extractRoles(claims);

        return new LoginResponse(
                accessToken,
                stringValue(tokenResponse.get("refresh_token")).orElse(null),
                stringValue(tokenResponse.get("token_type")).orElse("Bearer"),
                longValue(tokenResponse.get("expires_in")).orElse(0L),
                stringValue(claims.get("sub")).orElse(null),
                stringValue(claims.getOrDefault("preferred_username", claims.get("upn"))).orElse(fallbackUsername),
                stringValue(claims.get("email")).orElse(null),
                roles
        );
    }

    private String buildForm(String username, String password) {
        StringBuilder sb = new StringBuilder();
        append(sb, "client_id", config.clientId());
        append(sb, "grant_type", config.grantType());
        append(sb, "username", username);
        append(sb, "password", password);
        config.clientSecret().ifPresent(s -> append(sb, "client_secret", s));
        return sb.toString();
    }

    private String buildRefreshForm(String refreshToken) {
        StringBuilder sb = new StringBuilder();
        append(sb, "client_id", config.clientId());
        append(sb, "grant_type", "refresh_token");
        append(sb, "refresh_token", refreshToken);
        config.clientSecret().ifPresent(s -> append(sb, "client_secret", s));
        return sb.toString();
    }

    private void append(StringBuilder sb, String key, String value) {
        if (!sb.isEmpty()) sb.append('&');
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        sb.append('=');
        sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private Map<String, Object> parseJwtPayload(String jwt) throws IOException {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) throw new IOException("Invalid JWT format");
        byte[] decoded = Base64.getUrlDecoder().decode(pad(parts[1]));
        return objectMapper.readValue(decoded, MAP_TYPE);
    }

    private String pad(String value) {
        int remainder = value.length() % 4;
        return remainder == 0 ? value : value + "=".repeat(4 - remainder);
    }

    private List<String> extractRoles(Map<String, Object> claims) {
        Set<String> roles = new LinkedHashSet<>();
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmMap) {
            roles.addAll(toStringList(realmMap.get("roles")));
        }
        return List.copyOf(roles);
    }

    private List<String> toStringList(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof Collection<?> col) return col.stream().filter(Objects::nonNull).map(Object::toString).toList();
        return List.of(raw.toString());
    }

    private Optional<String> stringValue(Object v) { return v == null ? Optional.empty() : Optional.of(v.toString()); }
    private Optional<Long> longValue(Object v) {
        if (v == null) return Optional.empty();
        if (v instanceof Number n) return Optional.of(n.longValue());
        return Optional.of(Long.parseLong(v.toString()));
    }
}
