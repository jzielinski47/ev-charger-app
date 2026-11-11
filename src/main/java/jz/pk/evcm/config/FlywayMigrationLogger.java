package jz.pk.evcm.config;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class FlywayMigrationLogger implements Callback {

    private static final Logger log = LoggerFactory.getLogger(FlywayMigrationLogger.class);
    private static final Set<Event> HANDLED_EVENTS = EnumSet.of(
            Event.BEFORE_MIGRATE,
            Event.BEFORE_EACH_MIGRATE,
            Event.AFTER_EACH_MIGRATE,
            Event.AFTER_EACH_MIGRATE_ERROR,
            Event.AFTER_MIGRATE);

    private long migrationStartedAt;

    @Override
    public boolean supports(Event event, Context context) {
        return HANDLED_EVENTS.contains(event);
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        MigrationInfo migration = context.getMigrationInfo();

        switch (event) {
            case BEFORE_MIGRATE -> log.info("Database migration started");
            case BEFORE_EACH_MIGRATE -> {
                migrationStartedAt = System.nanoTime();
                log.info("Applying migration V{} - {}", migration.getVersion(), migration.getDescription());
            }
            case AFTER_EACH_MIGRATE -> log.info("Applied migration V{} - {} in {} ms",
                    migration.getVersion(), migration.getDescription(), elapsedMillis());
            case AFTER_EACH_MIGRATE_ERROR -> log.error("Migration V{} - {} failed after {} ms",
                    migration.getVersion(), migration.getDescription(), elapsedMillis());
            case AFTER_MIGRATE -> log.info("Database migration finished");
            default -> { }
        }
    }

    @Override
    public String getCallbackName() {
        return "migration-logger";
    }

    private long elapsedMillis() {
        return (System.nanoTime() - migrationStartedAt) / 1_000_000;
    }
}