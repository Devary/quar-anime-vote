package com.votescroll.dto;
public record VoterIdentity(String userId, String ipAddress) {
    public boolean isAuthenticated() { return userId != null; }
    public String voterKey() { return userId != null ? "user:" + userId : "ip:" + ipAddress; }
}
