package com.votescroll.resource;

import com.votescroll.dto.UserDto;
import com.votescroll.dto.UserUpdateDto;
import com.votescroll.service.UserService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/user/me")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Blocking
public class UserProfileResource {

    @Inject UserService   userService;
    @Inject JsonWebToken  jwt;

    @GET
    public UserDto getProfile() {
        return userService.getProfile(jwt.getSubject());
    }

    @PUT
    public UserDto updateProfile(UserUpdateDto dto) {
        return userService.updateProfile(jwt.getSubject(), dto);
    }
}
