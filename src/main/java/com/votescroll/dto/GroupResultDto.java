package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupResultDto {
    public String id;
    public String label;
    public List<CandidateResultDto> candidates;
    public long groupTotal;
}
