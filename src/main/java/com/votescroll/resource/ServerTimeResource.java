package com.votescroll.resource;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.Instant;
import java.util.Map;

@Path("/server-time")
@Produces(MediaType.APPLICATION_JSON)
@Blocking
public class ServerTimeResource {

    @GET
    @PermitAll
    public Response getServerTime() {
        return Response.ok(Map.of("now", Instant.now().toString())).build();
    }
}
