package com.votescroll.resource;

import com.votescroll.dto.CharacterCreateDto;
import com.votescroll.dto.CharacterDto;
import com.votescroll.service.CharacterAdminService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

@Path("/admin/characters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AdminCharacterResource {

    @Inject
    CharacterAdminService service;

    @GET
    @PermitAll
    public List<CharacterDto> listAll() {
        return service.listAll();
    }

    @POST
    @Authenticated
    public Response create(CharacterCreateDto req) {
        return Response.status(201).entity(service.create(req)).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    public CharacterDto update(@PathParam("id") String id, CharacterCreateDto req) {
        return service.update(id, req);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
