package com.votescroll.resource;

import com.votescroll.dto.HistoryItemDto;
import com.votescroll.service.HistoryService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "History", description = "Vote history by session")
@Slf4j
@Blocking
public class HistoryResource {

    @Inject
    HistoryService historyService;

    @GET
    @Operation(summary = "Get today's vote history for a session")
    public List<HistoryItemDto> getHistory(
            @QueryParam("sessionId") String sessionId,
            @QueryParam("date") String dateStr) {
        if (sessionId == null || sessionId.isBlank())
            throw new BadRequestException("sessionId is required");
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        return historyService.getHistory(sessionId, date);
    }
}
