package com.votescroll.entity;

public enum ContentStatus {
    PENDING,   // awaiting admin approval
    APPROVED,  // visible in main feed (or private poll usable by owner)
    REJECTED   // admin rejected, not visible anywhere
}
