package com.gymflow.tenancy.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

/**
 * Runs the tenant schema migrations against one gym's dedicated database.
 *
 * <p>Tenant migrations have their own location and history table, and this class refuses
 * to run them against the control-plane database, whether it is handed the control-plane
 * datasource itself or a separate datasource that points at the same database.
 */
public class TenantMigrations {

    public static final String LOCATION = "classpath:db/migration/tenant";
    public static final String HISTORY_TABLE = "flyway_tenant_history";

    private final DataSource controlPlaneDataSource;

    public TenantMigrations(DataSource controlPlaneDataSource) {
        this.controlPlaneDataSource = controlPlaneDataSource;
    }

    public MigrateResult migrate(DataSource tenantDataSource) {
        if (tenantDataSource == controlPlaneDataSource) {
            throw new IllegalArgumentException(
                "Refusing to run tenant migrations through the control-plane datasource");
        }
        if (containsControlPlaneHistory(tenantDataSource)) {
            throw new IllegalArgumentException(
                "Refusing to run tenant migrations: target database holds the control-plane schema");
        }
        return Flyway.configure()
            .dataSource(tenantDataSource)
            .locations(LOCATION)
            .table(HISTORY_TABLE)
            .failOnMissingLocations(true)
            .load()
            .migrate();
    }

    private static boolean containsControlPlaneHistory(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                    "SELECT to_regclass('" + ControlPlaneMigrations.HISTORY_TABLE + "') IS NOT NULL")) {
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not inspect the target tenant database", e);
        }
    }
}
