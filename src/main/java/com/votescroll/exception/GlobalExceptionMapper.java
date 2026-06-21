package com.votescroll.exception;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        if (status >= 500) log.error("Server error", e);
        else log.warn("Client error {}: {}", status, e.getMessage());
        return Response.status(status)
            .entity(new ErrorResponse(status, e.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
