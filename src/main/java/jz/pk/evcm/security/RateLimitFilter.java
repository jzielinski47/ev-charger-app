package jz.pk.evcm.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jz.pk.evcm.config.RateLimitProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RateLimitFilter extends OncePerRequestFilter {

    public static final String REMAINING_HEADER = "X-Rate-Limit-Remaining";

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final RateLimitProperties properties;
    private final JsonMapper jsonMapper;
    private final Cache<String, Bucket> buckets;

    public RateLimitFilter(RateLimitProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(properties.idleEviction())
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/auth/") || path.startsWith("/api/catalog/") || path.equals("/actuator/health") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

        if (authenticated && isAdmin(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = authenticated
                ? buckets.get("user:" + auth.getName(), key -> newBucket(properties.authenticatedCapacity()))
                : buckets.get("ip:" + request.getRemoteAddr(), key -> newBucket(properties.anonymousCapacity()));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        response.setHeader(REMAINING_HEADER, String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (probe.getNanosToWaitForRefill() + TimeUnit.SECONDS.toNanos(1) - 1)
                / TimeUnit.SECONDS.toNanos(1));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), Map.of(
                "error", "Too Many Requests",
                "message", "Rate limit exceeded, retry in " + retryAfterSeconds + " seconds"
        ));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> ADMIN_ROLE.equals(a.getAuthority()));
    }

    private Bucket newBucket(int capacity) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, properties.period()))
                .build();
    }
}