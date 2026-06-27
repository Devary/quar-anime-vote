package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PollResultDto {
    public PollDto poll;
    public List<FighterResultDto> fighterResults;
    public long total;
    public String myVoteCharId;
}
