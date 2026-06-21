package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.KeycloakPasswordGrantService;
import com.votescroll.service.LdapAuthService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AuthResource {

    @Inject KeycloakPasswordGrantService keycloakService;
    @Inject LdapAuthService ldapService;

    @POST @Path("/login") @PermitAll
    public Response login(LoginRequest req) {
        String username = ldapService.resolveUsername(req.username);
        return Response.ok(keycloakService.login(username, req.password)).build();
    }

    @POST @Path("/register") @PermitAll
    public Response register(RegisterRequest req) {
        ldapService.register(req);
        String username = ldapService.resolveUsername(req.username);
        return Response.status(201).entity(keycloakService.login(username, req.password)).build();
    }

    @POST @Path("/refresh") @PermitAll
    public Response refresh(RefreshRequest req) {
        return Response.ok(keycloakService.refresh(req.refreshToken)).build();
    }

    @GET @Path("/username-availability/{username}") @PermitAll
    public UsernameAvailabilityResponse usernameAvailability(@PathParam("username") String username) {
        return ldapService.checkAvailability(username);
    }
}
