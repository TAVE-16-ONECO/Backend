package com.oneco.backend.auth.application.dto;

public record TokensResponse(String accessToken, String refreshToken) {
}
