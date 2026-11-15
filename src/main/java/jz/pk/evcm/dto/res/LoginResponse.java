package jz.pk.evcm.dto.res;

public record LoginResponse(String token, long expiresIn, String refreshToken, long refreshExpiresIn) {}
