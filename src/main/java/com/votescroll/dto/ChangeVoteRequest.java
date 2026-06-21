package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ChangeVoteRequest {
    public String newCharacterId;
    public String sessionId;
}
