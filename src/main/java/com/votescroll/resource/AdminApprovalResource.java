package com.votescroll.resource;

import com.votescroll.dto.ApprovalSummaryDto;
import com.votescroll.service.ApprovalService;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/admin/approvals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class AdminApprovalResource {

    @Inject ApprovalService service;

    @GET
    public ApprovalSummaryDto summary() { return service.getSummary(); }

    // ── Content approvals ─────────────────────────────────────────────────────

    @POST @Path("/characters/{id}/approve")
    public Response approveChar(@PathParam("id") String id) {
        service.approveCharacter(id); return Response.ok().build();
    }

    @POST @Path("/characters/{id}/reject")
    public Response rejectChar(@PathParam("id") String id) {
        service.rejectCharacter(id); return Response.ok().build();
    }

    @POST @Path("/polls/{id}/approve")
    public Response approvePoll(@PathParam("id") String id) {
        service.approvePoll(id); return Response.ok().build();
    }

    @POST @Path("/polls/{id}/reject")
    public Response rejectPoll(@PathParam("id") String id) {
        service.rejectPoll(id); return Response.ok().build();
    }

    @POST @Path("/multi-polls/{id}/approve")
    public Response approveMp(@PathParam("id") String id) {
        service.approveMultiPoll(id); return Response.ok().build();
    }

    @POST @Path("/multi-polls/{id}/reject")
    public Response rejectMp(@PathParam("id") String id) {
        service.rejectMultiPoll(id); return Response.ok().build();
    }

    // ── Deletion approvals ────────────────────────────────────────────────────

    @POST @Path("/delete/polls/{id}/approve")
    public Response deletePollApprove(@PathParam("id") String id) {
        service.approvePollDeletion(id); return Response.ok().build();
    }

    @POST @Path("/delete/polls/{id}/reject")
    public Response deletePollReject(@PathParam("id") String id) {
        service.rejectPollDeletion(id); return Response.ok().build();
    }

    @POST @Path("/delete/multi-polls/{id}/approve")
    public Response deleteMpApprove(@PathParam("id") String id) {
        service.approveMultiPollDeletion(id); return Response.ok().build();
    }

    @POST @Path("/delete/multi-polls/{id}/reject")
    public Response deleteMpReject(@PathParam("id") String id) {
        service.rejectMultiPollDeletion(id); return Response.ok().build();
    }
}
