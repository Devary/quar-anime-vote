package com.votescroll.resource;

import com.votescroll.dto.AnimeCreateDto;
import com.votescroll.dto.AnimeDto;
import com.votescroll.service.AnimeAdminService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

@Path("/admin/anime")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AdminAnimeResource {

    @Inject AnimeAdminService service;

    @GET @PermitAll
    public List<AnimeDto> listAll() { return service.listAll(); }

    @GET @Path("/{id}") @PermitAll
    public AnimeDto getById(@PathParam("id") String id) { return service.getById(id); }

    @POST @RolesAllowed("ADMIN")
    public Response create(AnimeCreateDto req) {
        return Response.status(201).entity(service.create(req)).build();
    }

    @PUT @Path("/{id}") @RolesAllowed("ADMIN")
    public AnimeDto update(@PathParam("id") String id, AnimeCreateDto req) {
        return service.update(id, req);
    }

    @DELETE @Path("/{id}") @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
