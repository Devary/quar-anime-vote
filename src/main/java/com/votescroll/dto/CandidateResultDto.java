package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateResultDto {
    public String charId;
    public String name;
    public String imageUrl;
    public long votes;
    public double pct;  // within group
}
