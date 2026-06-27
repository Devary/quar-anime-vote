package com.votescroll.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.votescroll.dto.LoginResponse;
import com.votescroll.dto.RegisterRequest;
import com.votescroll.dto.UsernameAvailabilityResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RedzoneAuthClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public RedzoneAuthClient(
            @ConfigProperty(name = "redzone.url") String baseUrl,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public LoginResponse login(String username, String password) {
        return postForToken("/auth/login-test", Map.of("username", username, "password", password));
    }

    public LoginResponse register(RegisterRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", req.firstName != null ? req.firstName : req.username);
        body.put("lastName", req.lastName != null && !req.lastName.isBlank() ? req.lastName : req.username);
        body.put("email", req.email);
        body.put("username", req.username);
        body.put("password", req.password);
        body.put("confirmPassword", req.confirmPassword);
        body.put("acceptConditions", true);
        post("/auth/register", body);
        return login(req.username, req.password);
    }

    public LoginResponse refresh(String refreshToken) {
        return postForToken("/auth/refresh", Map.of("refreshToken", refreshToken));
    }

    public UsernameAvailabilityResponse checkAvailability(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/auth/username-availability/" + username))
                    .timeout(TIMEOUT)
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new WebApplicationException("Redzone availability check failed", response.statusCode());
            }
            Map<String, Object> map = objectMapper.readValue(response.body(), MAP_TYPE);
            return new UsernameAvailabilityResponse(
                    str(map.get("username")),
                    Boolean.TRUE.equals(map.get("available")),
                    str(map.get("message"))
            );
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Redzone auth service unavailable", e, Response.Status.BAD_GATEWAY);
        }
    }

    private LoginResponse postForToken(String path, Object body) {
        try {
            Map<String, Object> map = post(path, body);
            return new LoginResponse(
                    str(map.get("accessToken")),
                    str(map.get("refreshToken")),
                    str(map.getOrDefault("tokenType", "Bearer")),
                    longVal(map.get("expiresIn")),
                    str(map.get("subject")),
                    str(map.get("username")),
                    str(map.get("email")),
                    map.get("roles") instanceof List<?> roles
                            ? roles.stream().map(Object::toString).toList()
                            : List.of()
            );
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Redzone auth service unavailable", e, Response.Status.BAD_GATEWAY);
        }
    }

    private Map<String, Object> post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new WebApplicationException(response.body(), response.statusCode());
            }
            return objectMapper.readValue(response.body(), MAP_TYPE);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Redzone auth service unavailable", e, Response.Status.BAD_GATEWAY);
        }
    }

    private String str(Object v) { return v == null ? null : v.toString(); }

    private long longVal(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
