package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalItemDto {
    public String id;
    public String type;        // POLL | MULTI_POLL | CHARACTER
    public String title;       // question or character name
    public String anime;
    public String ownerId;
    public String ownerUsername;
    public String createdAt;   // ISO-8601
    public boolean isDeletion; // true when this is a deletion approval request
}
