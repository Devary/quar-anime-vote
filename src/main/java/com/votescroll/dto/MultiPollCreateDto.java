package com.votescroll.dto;

import com.votescroll.dto.validation.ValidGroupPeriod;
import com.votescroll.dto.validation.ValidGroupSequence;
import lombok.*;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@ValidGroupSequence
public class MultiPollCreateDto {
    public String anime;
    public String question;
    @Valid
    public List<GroupCreateDto> groups;

    @Data @NoArgsConstructor @AllArgsConstructor
    @ValidGroupPeriod
    public static class GroupCreateDto {
        public String label;
        public List<String> characterIds;
        public boolean startNow;
        public Instant startDate; // null when startNow=true
        public Instant endDate;   // always required on create
    }
}
