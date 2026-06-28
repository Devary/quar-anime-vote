package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.MultiPollAdminService;
import io.smallrye.common.annotation.Blocking;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/admin/multi-polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Blocking
public class AdminMultiPollResource {

    @Inject MultiPollAdminService service;

    @GET
    public List<MultiPollDto> listAll() { return service.listAll(); }

    @POST
    public Response create(@Valid MultiPollCreateDto req) {
        return Response.status(201).entity(service.create(req)).build();
    }

    @PUT @Path("/{id}")
    public MultiPollDto update(@PathParam("id") String id, MultiPollCreateDto req) {
        return service.update(id, req);
    }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
