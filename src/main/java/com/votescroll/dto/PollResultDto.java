package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PollResultDto {
    public PollDto poll;
    public long votes1;
    public long votes2;
    public double pct1;
    public double pct2;
    public long total;
    public String myVoteCharId;   // null if sessionId not provided or not voted
}
