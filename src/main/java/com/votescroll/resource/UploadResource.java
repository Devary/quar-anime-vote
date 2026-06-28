package com.votescroll.resource;

import com.votescroll.dto.UploadRequest;
import com.votescroll.dto.UploadResponse;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@jakarta.ws.rs.Path("")
@Blocking
public class UploadResource {

    @ConfigProperty(name = "upload.dir", defaultValue = "/opt/anime-vote/uploads")
    String uploadDir;

    @jakarta.ws.rs.core.Context
    jakarta.ws.rs.core.SecurityContext securityContext;

    @POST
    @jakarta.ws.rs.Path("/user/upload")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse uploadUserPicture(UploadRequest req) throws IOException {
        return saveFile(req);
    }

    @POST
    @jakarta.ws.rs.Path("/admin/upload")
    @jakarta.annotation.security.RolesAllowed("ADMIN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse upload(UploadRequest req) throws IOException {
        return saveFile(req);
    }

    private UploadResponse saveFile(UploadRequest req) throws IOException {
        if (req == null || req.data == null || req.data.isBlank()) {
            throw new WebApplicationException("No file data provided", Response.Status.BAD_REQUEST);
        }
        String ext = "";
        if (req.filename != null && req.filename.contains(".")) {
            String raw = req.filename.substring(req.filename.lastIndexOf('.'));
            if (raw.matches("\\.[a-zA-Z0-9]{1,6}")) ext = raw.toLowerCase();
        }
        String filename = UUID.randomUUID() + ext;
        byte[] bytes = Base64.getDecoder().decode(req.data);

        java.nio.file.Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), bytes);

        return new UploadResponse("/uploads/" + filename);
    }

    @GET
    @jakarta.ws.rs.Path("/uploads/{filename}")
    @PermitAll
    @Produces("*/*")
    public Response serveFile(@PathParam("filename") String filename) throws IOException {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Response.status(400).build();
        }
        java.nio.file.Path file = Paths.get(uploadDir).resolve(filename);
        if (!Files.exists(file)) {
            return Response.status(404).build();
        }
        String mime = Files.probeContentType(file);
        if (mime == null) mime = "application/octet-stream";

        return Response.ok(Files.readAllBytes(file))
                .header("Content-Type", mime)
                .header("Cache-Control", "public, max-age=86400")
                .build();
    }
}
