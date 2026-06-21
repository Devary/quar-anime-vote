package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VoteRequest {
    public String characterId;
    public String sessionId;
}
