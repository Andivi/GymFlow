package com.gymflow.tenancy.migration;

import javax.sql.DataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires Flyway by hand instead of using Spring Boot's single-datasource auto-configuration.
 *
 * <p>The application's primary {@link DataSource} is the control plane. Tenant datasources
 * are created per gym and are never registered as beans, so they cannot be picked up here.
 */
@Configuration
public class MigrationConfig {

    @Bean
    ControlPlaneMigrations controlPlaneMigrations(DataSource controlPlaneDataSource) {
        return new ControlPlaneMigrations(controlPlaneDataSource);
    }

    @Bean
    TenantMigrations tenantMigrations(DataSource controlPlaneDataSource) {
        return new TenantMigrations(controlPlaneDataSource);
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "gymflow.migrations.control.run-on-startup", matchIfMissing = true)
    InitializingBean controlPlaneMigrationOnStartup(ControlPlaneMigrations migrations) {
        return migrations::migrate;
    }
}
