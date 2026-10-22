package jz.pk.evcm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "charger.sync.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ChargerSyncProperties.class)
public class SchedulingConfig {
}
