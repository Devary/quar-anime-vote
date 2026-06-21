package com.votescroll.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class PollCreateDto {
    public String anime;
    public String question;
    public String fighter1Id;
    public String fighter2Id;
}
