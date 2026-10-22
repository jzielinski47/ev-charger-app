package jz.pk.evcm.scheduler;

import jz.pk.evcm.config.ChargerSyncProperties;
import jz.pk.evcm.service.OpenChargeApiService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "charger.sync.enabled", havingValue = "true", matchIfMissing = true)
@AllArgsConstructor
public class ChargerSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChargerSyncScheduler.class);

    private final OpenChargeApiService chargerService;
    private final ChargerSyncProperties props;

    @Scheduled(fixedDelay = 3, initialDelay = 1, timeUnit = TimeUnit.HOURS)
    public void syncChargers() {
        log.info("Charger sync started");
        try {
            var saved = chargerService.fetchChargersAndSave(
                    props.latitude(), props.longitude(), props.distanceInKm(), props.maxResults());
            log.info("Charger sync finished, saved {} chargers", saved.size());
        } catch (Exception e) {
            log.error("Charger sync failed", e);
        }
    }


}
