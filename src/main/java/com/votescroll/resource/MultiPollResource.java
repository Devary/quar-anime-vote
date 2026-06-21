package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.MultiPollService;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/multi-polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Multi-Polls", description = "Multi-candidate poll endpoints")
@Slf4j
@Blocking
public class MultiPollResource {

    @Inject
    MultiPollService multiPollService;

    @Context SecurityContext security;
    @Context HttpServerRequest vertxRequest;

    private VoterIdentity voterIdentity() {
        String userId = null;
        if (security.getUserPrincipal() != null && !security.getUserPrincipal().getName().equals("ANONYMOUS")) {
            userId = security.getUserPrincipal().getName();
        }
        String ip = java.util.Optional.ofNullable(vertxRequest.getHeader("X-Forwarded-For"))
            .map(h -> h.split(",")[0].trim())
            .orElse(vertxRequest.remoteAddress().host());
        return new VoterIdentity(userId, ip);
    }

    @GET
    @PermitAll
    @Operation(summary = "List all multi-polls")
    public List<MultiPollDto> listAll() {
        return multiPollService.getAll();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    @Operation(summary = "Get multi-poll results")
    public MultiPollResultDto getResult(@PathParam("id") String id) {
        return multiPollService.getResult(id, voterIdentity());
    }

    @POST
    @Path("/{id}/vote")
    @PermitAll
    @Operation(summary = "Vote on a multi-poll")
    public Response vote(@PathParam("id") String id, VoteRequest req) {
        return Response.ok(multiPollService.vote(id, req, voterIdentity())).build();
    }

    @PUT
    @Path("/{id}/vote")
    @PermitAll
    @Operation(summary = "Change your vote on a multi-poll")
    public Response changeVote(@PathParam("id") String id, ChangeVoteRequest req) {
        return Response.ok(multiPollService.changeVote(id, req, voterIdentity())).build();
    }
}
