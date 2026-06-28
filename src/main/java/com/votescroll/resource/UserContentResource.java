package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.UserContentService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Blocking
public class UserContentResource {

    @Inject UserContentService service;
    @Inject JsonWebToken jwt;

    private String userId() { return jwt.getSubject(); }

    // ── Daily limits ──────────────────────────────────────────────────────────

    @GET @Path("/limits")
    public DailyLimitDto limits() { return service.getDailyLimits(userId()); }

    // ── Characters ────────────────────────────────────────────────────────────

    @GET @Path("/characters")
    public List<CharacterDto> myCharacters() { return service.listMyCharacters(userId()); }

    @POST @Path("/characters")
    public Response createCharacter(CharacterCreateDto req) {
        return Response.status(201).entity(service.createCharacter(req, userId())).build();
    }

    @PUT @Path("/characters/{id}")
    public CharacterDto updateCharacter(@PathParam("id") String id, CharacterCreateDto req) {
        return service.updateCharacter(id, req, userId());
    }

    @DELETE @Path("/characters/{id}")
    public Response deleteCharacter(@PathParam("id") String id) {
        service.deleteCharacter(id, userId());
        return Response.noContent().build();
    }

    // ── Polls ─────────────────────────────────────────────────────────────────

    @GET @Path("/polls")
    public List<PollDto> myPolls() { return service.listMyPolls(userId()); }

    @POST @Path("/polls")
    public Response createPoll(PollCreateDto req) {
        return Response.status(201).entity(service.createPoll(req, userId())).build();
    }

    @PUT @Path("/polls/{id}")
    public PollDto updatePoll(@PathParam("id") String id, PollCreateDto req) {
        return service.updatePoll(id, req, userId());
    }

    @DELETE @Path("/polls/{id}")
    public Response deletePoll(@PathParam("id") String id) {
        service.deletePoll(id, userId());
        return Response.noContent().build();
    }

    // ── Multi-polls ───────────────────────────────────────────────────────────

    @GET @Path("/multi-polls")
    public List<MultiPollDto> myMultiPolls() { return service.listMyMultiPolls(userId()); }

    @POST @Path("/multi-polls")
    public Response createMultiPoll(MultiPollCreateDto req) {
        return Response.status(201).entity(service.createMultiPoll(req, userId())).build();
    }

    @PUT @Path("/multi-polls/{id}")
    public MultiPollDto updateMultiPoll(@PathParam("id") String id, MultiPollCreateDto req) {
        return service.updateMultiPoll(id, req, userId());
    }

    @DELETE @Path("/multi-polls/{id}")
    public Response deleteMultiPoll(@PathParam("id") String id) {
        service.deleteMultiPoll(id, userId());
        return Response.noContent().build();
    }
}
