package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.MultiPollService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @GET
    @Operation(summary = "List all multi-polls")
    public List<MultiPollDto> listAll() {
        return multiPollService.getAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get multi-poll results")
    public MultiPollResultDto getResult(
            @PathParam("id") String id,
            @QueryParam("sessionId") String sessionId) {
        return multiPollService.getResult(id, sessionId);
    }

    @POST
    @Path("/{id}/vote")
    @Operation(summary = "Vote on a multi-poll")
    public Response vote(@PathParam("id") String id, VoteRequest req) {
        return Response.ok(multiPollService.vote(id, req)).build();
    }

    @PUT
    @Path("/{id}/vote")
    @Operation(summary = "Change your vote on a multi-poll")
    public Response changeVote(@PathParam("id") String id, ChangeVoteRequest req) {
        return Response.ok(multiPollService.changeVote(id, req)).build();
    }
}
