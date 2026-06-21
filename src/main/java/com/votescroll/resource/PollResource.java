package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.PollService;
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

@Path("/polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Polls", description = "Single 1v1 poll endpoints")
@Slf4j
@Blocking
public class PollResource {

    @Inject
    PollService pollService;

    @Context SecurityContext security;
    @Context HttpServerRequest vertxRequest;

    private VoterIdentity voterIdentity() {
        Long userId = null;
        if (security.getUserPrincipal() != null) {
            try { userId = Long.parseLong(security.getUserPrincipal().getName()); } catch (NumberFormatException ignored) {}
        }
        String ip = java.util.Optional.ofNullable(vertxRequest.getHeader("X-Forwarded-For"))
            .map(h -> h.split(",")[0].trim())
            .orElse(vertxRequest.remoteAddress().host());
        return new VoterIdentity(userId, ip);
    }

    @GET
    @PermitAll
    @Operation(summary = "List all 1v1 polls")
    public List<PollDto> listAll() {
        return pollService.getAll();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    @Operation(summary = "Get poll result by ID")
    public PollResultDto getResult(@PathParam("id") String id) {
        return pollService.getResult(id, voterIdentity());
    }

    @POST
    @Path("/{id}/vote")
    @PermitAll
    @Operation(summary = "Cast a vote on a poll")
    public Response vote(@PathParam("id") String id, VoteRequest req) {
        return Response.ok(pollService.vote(id, req, voterIdentity())).build();
    }

    @PUT
    @Path("/{id}/vote")
    @PermitAll
    @Operation(summary = "Change your vote on a poll")
    public Response changeVote(@PathParam("id") String id, ChangeVoteRequest req) {
        return Response.ok(pollService.changeVote(id, req, voterIdentity())).build();
    }
}
