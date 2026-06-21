package com.votescroll.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoryItemDto {
    public String pollId;
    public String pollType;     // "single" or "multi"
    public String anime;
    public String question;
    public String myVoteCharId;
    public String myVoteCharName;
    public LocalDateTime votedAt;
}
