package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyLimitDto {
    public static final int DAILY_MAX = 5;
    public long charactersToday;
    public long pollsToday;
    public long multiPollsToday;
    public int remaining(long today) { return (int) Math.max(0, DAILY_MAX - today); }
}
