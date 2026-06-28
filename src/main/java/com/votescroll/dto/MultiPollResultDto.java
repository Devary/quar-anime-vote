package com.votescroll.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollResultDto {
    public MultiPollDto poll;
    public List<GroupResultDto> groups;
    public String overallWinnerCharId;
    /** groupId → charId the current user voted for */
    public Map<String, String> myVotesByGroup;
}
