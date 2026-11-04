package com.finsight.backend.auth;

public class AuthDTOs {
    public record RegisterRequest(String email, String password, String fullName) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token, String email, String fullName, String role) {}
    public record UserProfileResponse(String email, String fullName, String role) {}
}