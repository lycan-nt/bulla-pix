package com.bullla.pix.api.dto;

public record TokenResponse(
        String accessToken,
        String tokenType
) {
}
