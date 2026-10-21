package com.gymflow.tenancy.migration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

/**
 * Runs the control-plane schema migrations against the shared control-plane database.
 */
public class ControlPlaneMigrations {

    public static final String LOCATION = "classpath:db/migration/control";
    public static final String HISTORY_TABLE = "flyway_control_history";

    private final Flyway flyway;

    public ControlPlaneMigrations(DataSource controlPlaneDataSource) {
        this.flyway = Flyway.configure()
            .dataSource(controlPlaneDataSource)
            .locations(LOCATION)
            .table(HISTORY_TABLE)
            .failOnMissingLocations(true)
            .load();
    }

    public MigrateResult migrate() {
        return flyway.migrate();
    }
}
