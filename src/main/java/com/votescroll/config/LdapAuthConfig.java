package com.votescroll.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "anime.ldap")
public interface LdapAuthConfig {
    String url();
    String baseDn();
    String bindDn();
    String bindPassword();
    @WithDefault("people")
    String peopleOu();
}
