package com.votescroll.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@ConfigMapping(prefix = "anime.keycloak")
public interface KeycloakAuthConfig {
    String tokenUrl();
    @WithDefault("anime-vote")
    String clientId();
    @WithDefault("password")
    String grantType();
    Optional<String> clientSecret();
}
