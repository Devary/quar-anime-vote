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
    public boolean isPrivate;
    @Valid
    public List<GroupCreateDto> groups;

    @Data @NoArgsConstructor @AllArgsConstructor
    @ValidGroupPeriod
    public static class GroupCreateDto {
        public String label;
        public List<String> characterIds; // empty for level 1+ groups (candidates resolved from winners)
        public boolean startNow;
        public Instant startDate; // null when startNow=true or level > 0
        public Instant endDate;   // always required
        public int level;         // 0 = base level; 1+ = bracket levels
        /** Indices into the parent groups list whose winners compete here (level 1+ only) */
        public List<Integer> feederIndices;
    }
}
