package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollResultDto {
    public MultiPollDto poll;
    public List<GroupResultDto> groups;
    public String overallWinnerCharId;
    public String myVoteCharId;
}
