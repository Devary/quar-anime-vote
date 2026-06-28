package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalSummaryDto {
    public List<ApprovalItemDto> pendingContent;
    public List<ApprovalItemDto> pendingDeletions;
}
