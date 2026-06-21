package com.votescroll.service;

import com.votescroll.config.LdapAuthConfig;
import com.votescroll.dto.RegisterRequest;
import com.votescroll.dto.UsernameAvailabilityResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.Locale;

@ApplicationScoped
public class LdapAuthService {

    private final LdapAuthConfig config;

    @Inject
    public LdapAuthService(LdapAuthConfig config) {
        this.config = config;
    }

    public UsernameAvailabilityResponse checkAvailability(String rawUsername) {
        String username = normalize(rawUsername);
        if (username.length() < 3) {
            return new UsernameAvailabilityResponse(username, false, "Username must be at least 3 characters.");
        }
        DirContext ctx = null;
        try {
            ctx = openContext();
            boolean exists = findByUsername(ctx, username) != null;
            return new UsernameAvailabilityResponse(username, !exists,
                    exists ? "Username already exists." : "Username is available.");
        } catch (NamingException e) {
            throw new WebApplicationException("LDAP error checking availability", e, Response.Status.BAD_GATEWAY);
        } finally {
            close(ctx);
        }
    }

    public String register(RegisterRequest req) {
        String username = normalize(req.username);
        String email = req.email.trim().toLowerCase(Locale.ROOT);

        if (req.password == null || req.password.length() < 8) {
            throw new WebApplicationException("Password must be at least 8 characters", Response.Status.BAD_REQUEST);
        }
        if (!req.password.equals(req.confirmPassword)) {
            throw new WebApplicationException("Passwords do not match", Response.Status.BAD_REQUEST);
        }

        DirContext ctx = null;
        try {
            ctx = openContext();
            ensurePeopleOu(ctx);

            if (findByUsername(ctx, username) != null) {
                throw new WebApplicationException("Username already exists", Response.Status.CONFLICT);
            }
            if (findByEmail(ctx, email) != null) {
                throw new WebApplicationException("Email already registered", Response.Status.CONFLICT);
            }

            String dn = userDn(username);
            ctx.bind(dn, null, buildAttributes(username, email, req.firstName, req.lastName));
            setPassword(ctx, dn, req.password);
            return username;
        } catch (WebApplicationException e) {
            throw e;
        } catch (NamingException e) {
            throw new WebApplicationException("LDAP registration failed", e, Response.Status.BAD_GATEWAY);
        } finally {
            close(ctx);
        }
    }

    public String resolveUsername(String identifier) {
        String normalized = identifier.trim();
        if (!normalized.contains("@")) return normalized;
        DirContext ctx = null;
        try {
            ctx = openContext();
            SearchResult result = findByEmail(ctx, normalized.toLowerCase(Locale.ROOT));
            if (result == null) return normalized;
            Attribute uid = result.getAttributes().get("uid");
            return uid == null ? normalized : uid.get().toString();
        } catch (NamingException e) {
            throw new WebApplicationException("LDAP error resolving login", e, Response.Status.BAD_GATEWAY);
        } finally {
            close(ctx);
        }
    }

    private Attributes buildAttributes(String username, String email, String firstName, String lastName) {
        Attributes attrs = new BasicAttributes(true);
        BasicAttribute oc = new BasicAttribute("objectClass");
        oc.add("top"); oc.add("person"); oc.add("organizationalPerson"); oc.add("inetOrgPerson");
        attrs.put(oc);
        attrs.put("uid", username);
        attrs.put("cn", firstName + " " + lastName);
        attrs.put("givenName", firstName);
        attrs.put("sn", lastName.isBlank() ? username : lastName);
        attrs.put("mail", email);
        return attrs;
    }

    private void setPassword(DirContext ctx, String dn, String password) throws NamingException {
        ModificationItem[] mods = { new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", password)) };
        ctx.modifyAttributes(dn, mods);
    }

    private void ensurePeopleOu(DirContext ctx) throws NamingException {
        String dn = peopleOuDn();
        try {
            ctx.getAttributes(dn);
        } catch (NameNotFoundException e) {
            Attributes attrs = new BasicAttributes(true);
            BasicAttribute oc = new BasicAttribute("objectClass");
            oc.add("top"); oc.add("organizationalUnit");
            attrs.put(oc);
            attrs.put("ou", config.peopleOu());
            ctx.createSubcontext(dn, attrs).close();
        }
    }

    private SearchResult findByUsername(DirContext ctx, String username) throws NamingException {
        return findSingle(ctx, "(uid=" + escapeFilter(username) + ")");
    }

    private SearchResult findByEmail(DirContext ctx, String email) throws NamingException {
        return findSingle(ctx, "(mail=" + escapeFilter(email) + ")");
    }

    private SearchResult findSingle(DirContext ctx, String filter) throws NamingException {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        sc.setCountLimit(1);
        NamingEnumeration<SearchResult> results = ctx.search(peopleOuDn(), filter, sc);
        try {
            return results.hasMore() ? results.next() : null;
        } finally {
            results.close();
        }
    }

    private DirContext openContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, config.url());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, config.bindDn());
        env.put(Context.SECURITY_CREDENTIALS, config.bindPassword());
        env.put("com.sun.jndi.ldap.connect.timeout", "3000");
        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        return new InitialDirContext(env);
    }

    private void close(DirContext ctx) {
        if (ctx != null) try { ctx.close(); } catch (NamingException ignored) {}
    }

    private String normalize(String username) { return username.trim().toLowerCase(Locale.ROOT); }
    private String peopleOuDn() { return "ou=" + config.peopleOu() + "," + config.baseDn(); }
    private String userDn(String username) { return "uid=" + username + "," + peopleOuDn(); }

    private String escapeFilter(String value) {
        return value.replace("\\", "\\5c").replace("*", "\\2a").replace("(", "\\28").replace(")", "\\29").replace("\0", "\\00");
    }
}
