package jz.pk.evcm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("5") int anonymousCapacity,
        @DefaultValue("20") int authenticatedCapacity,
        @DefaultValue("1m") Duration period,
        @DefaultValue("2m") Duration idleEviction
) {
}
