package com.votescroll.resource;

import com.votescroll.dto.HistoryItemDto;
import com.votescroll.dto.VoterIdentity;
import com.votescroll.service.HistoryService;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "History", description = "Vote history by voter identity")
@Slf4j
@Blocking
public class HistoryResource {

    @Inject
    HistoryService historyService;

    @Context SecurityContext security;
    @Context HttpServerRequest vertxRequest;

    private VoterIdentity voterIdentity() {
        String userId = null;
        if (security.getUserPrincipal() != null && !security.getUserPrincipal().getName().equals("ANONYMOUS")) {
            userId = security.getUserPrincipal().getName();
        }
        String ip = java.util.Optional.ofNullable(vertxRequest.getHeader("X-Forwarded-For"))
            .map(h -> h.split(",")[0].trim())
            .orElse(vertxRequest.remoteAddress().host());
        return new VoterIdentity(userId, ip);
    }

    @GET
    @PermitAll
    @Operation(summary = "Get today's vote history for current voter")
    public List<HistoryItemDto> getHistory(@QueryParam("date") String dateStr) {
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        return historyService.getHistory(voterIdentity(), date);
    }
}
