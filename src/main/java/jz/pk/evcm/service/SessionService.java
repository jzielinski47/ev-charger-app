package jz.pk.evcm.service;

import jz.pk.evcm.dto.res.LoginResponse;
import jz.pk.evcm.entity.RefreshToken;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.exception.InvalidRefreshTokenException;
import jz.pk.evcm.repository.RefreshTokenRepository;
import jz.pk.evcm.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Transactional
public class SessionService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final Duration refreshExpiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(RefreshTokenRepository refreshTokenRepository,
                          JwtService jwtService,
                          @Value("${security.jwt.refresh-expiration:30d}") Duration refreshExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshExpiration = refreshExpiration;
    }

    public LoginResponse startSession(User user) {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpiredByUser(user, now);

        String rawToken = newRawToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(now.plus(refreshExpiration))
                .build());

        return new LoginResponse(jwtService.generateToken(user), jwtService.getExpirationTime(),
                rawToken, refreshExpiration.toMillis());
    }

    public LoginResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshTokenRepository.deleteByIdReturningCount(stored.getId()) == 0
                || stored.isExpired(Instant.now())
                || !stored.getUser().isAccountNonLocked()) {
            throw new InvalidRefreshTokenException();
        }

        return startSession(stored.getUser());
    }

    public void endSession(String rawToken) {
        refreshTokenRepository.deleteByTokenHash(hash(rawToken));
    }

    public void endAllSessions(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String newRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
