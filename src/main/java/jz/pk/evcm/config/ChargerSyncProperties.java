package jz.pk.evcm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "charger.sync")
public record ChargerSyncProperties(
        double latitude,
        double longitude,
        double distanceInKm,
        int maxResults
) {
}