package com.votescroll.resource;

import com.votescroll.dto.*;
import com.votescroll.service.UserService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class AdminUserResource {

    @Inject UserService userService;

    @GET
    public List<UserDto> listAll() {
        return userService.listAllUsers();
    }

    @PUT @Path("/{id}")
    public UserDto update(@PathParam("id") String id, AdminUserUpdateDto dto) {
        return userService.adminUpdateUser(id, dto);
    }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }
}
