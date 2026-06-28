package com.votescroll.resource;

import com.votescroll.dto.RoleCreateDto;
import com.votescroll.dto.RoleDto;
import com.votescroll.service.UserService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

@Path("/admin/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class AdminRoleResource {

    @Inject UserService userService;

    @GET
    public List<RoleDto> listAll() {
        return userService.listAllRoles();
    }

    @POST
    public Response create(RoleCreateDto dto) {
        return Response.status(201).entity(userService.createRole(dto)).build();
    }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        userService.deleteRole(id);
        return Response.noContent().build();
    }
}
