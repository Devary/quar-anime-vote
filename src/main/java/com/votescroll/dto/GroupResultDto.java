package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupResultDto {
    public String id;
    public String label;
    public int level;
    public List<String> feederGroupIds;
    public boolean resolved;
    public List<CandidateResultDto> candidates;
    public long groupTotal;
}
