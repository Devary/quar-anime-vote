package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.PollService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @GET
    @Operation(summary = "List all 1v1 polls")
    public List<PollDto> listAll() {
        return pollService.getAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get poll result by ID")
    public PollResultDto getResult(
            @PathParam("id") String id,
            @QueryParam("sessionId") String sessionId) {
        return pollService.getResult(id, sessionId);
    }

    @POST
    @Path("/{id}/vote")
    @Operation(summary = "Cast a vote on a poll")
    public Response vote(@PathParam("id") String id, VoteRequest req) {
        return Response.ok(pollService.vote(id, req)).build();
    }

    @PUT
    @Path("/{id}/vote")
    @Operation(summary = "Change your vote on a poll")
    public Response changeVote(@PathParam("id") String id, ChangeVoteRequest req) {
        return Response.ok(pollService.changeVote(id, req)).build();
    }
}
