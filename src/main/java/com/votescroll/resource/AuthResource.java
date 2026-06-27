package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.RedzoneAuthClient;
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

    @Inject RedzoneAuthClient redzoneAuth;

    @POST @Path("/login") @PermitAll
    public Response login(LoginRequest req) {
        return Response.ok(redzoneAuth.login(req.username, req.password)).build();
    }

    @POST @Path("/register") @PermitAll
    public Response register(RegisterRequest req) {
        return Response.status(201).entity(redzoneAuth.register(req)).build();
    }

    @POST @Path("/refresh") @PermitAll
    public Response refresh(RefreshRequest req) {
        return Response.ok(redzoneAuth.refresh(req.refreshToken)).build();
    }

    @GET @Path("/username-availability/{username}") @PermitAll
    public UsernameAvailabilityResponse usernameAvailability(@PathParam("username") String username) {
        return redzoneAuth.checkAvailability(username);
    }
}
