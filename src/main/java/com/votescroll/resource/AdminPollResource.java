package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.PollAdminService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/admin/polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN", "user", "admin"})
@Blocking
public class AdminPollResource {

    @Inject PollAdminService service;

    @GET
    public List<PollDto> listAll() { return service.listAll(); }

    @GET @Path("/characters")
    public List<CharacterDto> characters() { return service.listCharacters(); }

    @POST
    public Response create(PollCreateDto req) {
        return Response.status(201).entity(service.create(req)).build();
    }

    @PUT @Path("/{id}")
    public PollDto update(@PathParam("id") String id, PollCreateDto req) {
        return service.update(id, req);
    }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
