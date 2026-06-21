package com.votescroll.dto;

public record UsernameAvailabilityResponse(String username, boolean available, String message) {}
