package com.votescroll.service;

import com.votescroll.dto.LoginResponse;
import com.votescroll.entity.AppUser;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TokenService {

    private static final Duration ACCESS_TTL  = Duration.ofHours(1);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "anime-vote")
    String issuer;

    @Inject JWTParser jwtParser;

    public LoginResponse issueTokens(AppUser user) {
        Set<String> roleIds = user.roles.stream().map(r -> r.id).collect(Collectors.toSet());
        String access  = sign(user, ACCESS_TTL,  "access",  roleIds);
        String refresh = sign(user, REFRESH_TTL, "refresh", roleIds);
        List<String> roleList = new ArrayList<>(roleIds);
        return new LoginResponse(access, refresh, "Bearer", ACCESS_TTL.toSeconds(),
                user.id, user.username, user.email, roleList);
    }

    public AppUser verifyRefreshAndGetUser(String token) {
        try {
            JsonWebToken jwt = jwtParser.parse(token);
            if (!"refresh".equals(jwt.<String>getClaim("token_use"))) {
                throw new WebApplicationException("Not a refresh token", Response.Status.UNAUTHORIZED);
            }
            String userId = jwt.getSubject();
            AppUser user = AppUser.findById(userId);
            if (user == null) throw new WebApplicationException("User not found", Response.Status.UNAUTHORIZED);
            return user;
        } catch (ParseException e) {
            throw new WebApplicationException("Invalid or expired refresh token", Response.Status.UNAUTHORIZED);
        }
    }

    private String sign(AppUser user, Duration ttl, String tokenUse, Set<String> groups) {
        return Jwt.issuer(issuer)
                .subject(user.id)
                .upn(user.username)
                .claim("username", user.username)
                .claim("email",    user.email)
                .claim("token_use", tokenUse)
                .groups(groups)
                .expiresIn(ttl)
                .sign();
    }
}
